// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import soil.query.SwrCachePolicy.Companion.DEFAULT_GC_CHUNK_SIZE
import soil.query.SwrCachePolicy.Companion.DEFAULT_GC_INTERVAL
import soil.query.internal.ActorBlockRunner
import soil.query.internal.ActorSequenceNumber
import soil.query.internal.MemoryPressure
import soil.query.internal.MemoryPressureLevel
import soil.query.internal.NetworkConnectivity
import soil.query.internal.NetworkConnectivityEvent
import soil.query.internal.SurrogateKey
import soil.query.internal.TimeBasedCache
import soil.query.internal.UniqueId
import soil.query.internal.WindowVisibility
import soil.query.internal.WindowVisibilityEvent
import soil.query.internal.chunkedWithTimeout
import soil.query.internal.epoch
import soil.query.internal.vvv
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of the [SwrClient] interface.
 *
 * [Query] internally manages two categories:
 * - Active: When there is one or more references or within the [QueryOptions.keepAliveTime] period
 * - Inactive: When there are no references and past the [QueryOptions.keepAliveTime] period
 *
 * [Query] in the Active state does not disappear from memory unless one of the following conditions is met:
 * - [vacuum] is executed due to memory pressure
 * - [removeQueries] is explicitly called
 *
 * On the other hand, [Query] in the Inactive state gradually disappears from memory when one of the following conditions is met:
 * - Exceeds the maximum retention count of [TimeBasedCache]
 * - Past the [QueryOptions.gcTime] period since saved in [TimeBasedCache]
 * - [evictCache] or [clearCache] is executed for unnecessary memory release
 *
 * [Mutation] is managed similarly to Active state [Query], but it is not explicitly deleted like [removeQueries].
 * Typically, since the result of [Mutation] execution is not reused, it does not cache after going inactive.
 *
 * @param policy The policy for the [SwrCache].
 * @constructor Creates a new [SwrCache] instance.
 */
class SwrCache(private val policy: SwrCachePolicy) : SwrClient, QueryMutableClient {

    constructor(coroutineScope: CoroutineScope) : this(SwrCachePolicy(coroutineScope))

    private val mutationReceiver = policy.mutationReceiver
    private val mutationStore: MutableMap<UniqueId, ManagedMutation<*>> = policy.mutationStore
    private val queryReceiver = policy.queryReceiver
    private val queryStore: MutableMap<UniqueId, ManagedQuery<*>> = policy.queryStore
    private val queryCache: TimeBasedCache<UniqueId, QueryState<*>> = policy.queryCache

    private val coroutineScope: CoroutineScope = CoroutineScope(
        context = newCoroutineContext(policy.coroutineScope)
    )

    private val gcFlow: MutableSharedFlow<() -> Unit> = MutableSharedFlow()

    private var mountedIds: Set<String> = emptySet()
    private var mountedScope: CoroutineScope? = null

    init {
        gcFlow
            .chunkedWithTimeout(size = policy.gcChunkSize, duration = policy.gcInterval)
            .onEach { actions ->
                withContext(policy.mainDispatcher) {
                    actions.forEach { it() }
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     * Releases data in memory based on the specified [level].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun gc(level: MemoryPressureLevel = MemoryPressureLevel.Low) {
        when (level) {
            MemoryPressureLevel.Low -> coroutineScope.launch { evictCache() }
            MemoryPressureLevel.High -> coroutineScope.launch { clearCache() }
            MemoryPressureLevel.Critical -> coroutineScope.launch { vacuum() }
        }
    }

    private fun evictCache() {
        queryCache.evict()
    }

    private fun clearCache() {
        queryCache.clear()
    }

    private fun vacuum() {
        clearCache()
        // NOTE: Releases items that are active due to keepAliveTime but have no subscribers.
        queryStore.keys.toSet()
            .asSequence()
            .filter { id -> queryStore[id]?.ping()?.not() ?: false }
            .forEach { id -> queryStore.remove(id)?.close() }
        mutationStore.keys.toSet()
            .asSequence()
            .filter { id -> mutationStore[id]?.ping()?.not() ?: false }
            .forEach { id -> mutationStore.remove(id)?.close() }
    }

    // ----- SwrClient ----- //
    override val defaultMutationOptions: MutationOptions = policy.mutationOptions
    override val defaultQueryOptions: QueryOptions = policy.queryOptions

    override fun perform(sideEffects: QueryEffect) {
        coroutineScope.launch {
            with(this@SwrCache) { sideEffects() }
        }
    }

    override fun onMount(id: String) {
        if (mountedIds.isEmpty()) {
            val scope = CoroutineScope(context = newCoroutineContext(coroutineScope))
            scope.launch { observeMemoryPressure() }
            scope.launch { observeNetworkConnectivity() }
            scope.launch { observeWindowVisibility() }
            mountedScope = scope
        }
        mountedIds += id
    }

    override fun onUnmount(id: String) {
        mountedIds -= id
        if (mountedIds.isEmpty()) {
            mountedScope?.cancel()
            mountedScope = null
        }
    }

    private suspend fun observeMemoryPressure() {
        if (policy.memoryPressure == MemoryPressure.Unsupported) return
        policy.memoryPressure.asFlow()
            .collect(::gc)
    }

    private suspend fun observeNetworkConnectivity() {
        if (policy.networkConnectivity == NetworkConnectivity.Unsupported) return
        policy.networkConnectivity.asFlow()
            .distinctUntilChanged()
            .scan(NetworkConnectivityEvent.Available to NetworkConnectivityEvent.Available) { acc, state -> state to acc.first }
            .filter { it.first == NetworkConnectivityEvent.Available && it.second == NetworkConnectivityEvent.Lost }
            .onEach { delay(policy.networkResumeAfterDelay) }
            .collect {
                perform {
                    forEach(policy.networkResumeQueriesFilter) { id, _ ->
                        queryStore[id]
                            ?.takeIf { it.options.revalidateOnReconnect }
                            ?.resume()
                    }
                }
            }
    }

    private suspend fun observeWindowVisibility() {
        if (policy.windowVisibility == WindowVisibility.Unsupported) return
        policy.windowVisibility.asFlow()
            .distinctUntilChanged()
            .scan(WindowVisibilityEvent.Foreground to WindowVisibilityEvent.Foreground) { acc, state -> state to acc.first }
            .filter { it.first == WindowVisibilityEvent.Foreground && it.second == WindowVisibilityEvent.Background }
            .collect {
                perform {
                    forEach(policy.windowResumeQueriesFilter) { id, _ ->
                        queryStore[id]
                            ?.takeIf { it.options.revalidateOnFocus }
                            ?.resume()
                    }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(
        key: MutationKey<T, S>
    ): MutationRef<T, S> {
        val id = key.id
        val options = key.onConfigureOptions()?.invoke(defaultMutationOptions) ?: defaultMutationOptions
        var mutation = mutationStore[id] as? ManagedMutation<T>
        if (mutation == null) {
            mutation = newMutation(
                id = id,
                options = options,
                initialValue = MutationState<T>()
            ).also { mutationStore[id] = it }
        }
        return MutationRef(
            key = key,
            options = options,
            mutation = mutation
        )
    }

    private fun <T> newMutation(
        id: UniqueId,
        options: MutationOptions,
        initialValue: MutationState<T>
    ): ManagedMutation<T> {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope))
        val event = MutableSharedFlow<MutationEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val state = MutableStateFlow(initialValue)
        val reducer = createMutationReducer<T>()
        val dispatch: MutationDispatch<T> = { action ->
            options.vvv(id) { "dispatching $action" }
            state.value = reducer(state.value, action)
        }
        val notifier = MutationNotifier { effect -> perform(effect) }
        val command = Channel<MutationCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = { seq ->
                scope.launch { gcFlow.emit { closeMutation<T>(id, seq) } }
            }
        ) {
            for (c in command) {
                options.vvv(id) { "next command $c" }
                c.handle(
                    ctx = ManagedMutationContext(
                        receiver = mutationReceiver,
                        options = options,
                        state = state.value,
                        dispatch = dispatch,
                        notifier = notifier
                    )
                )
            }
        }
        return ManagedMutation(
            id = id,
            options = options,
            scope = scope,
            dispatch = dispatch,
            actor = actor,
            event = event,
            state = state,
            command = command
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> closeMutation(id: UniqueId, seq: ActorSequenceNumber) {
        val mutation = mutationStore[id] as? ManagedMutation<T> ?: return
        if (mutation.actor.seq == seq) {
            mutationStore.remove(id)
            mutation.close()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(key: QueryKey<T>): QueryRef<T> {
        val id = key.id
        val options = key.onConfigureOptions()?.invoke(defaultQueryOptions) ?: defaultQueryOptions
        var query = queryStore[id] as? ManagedQuery<T>
        if (query == null) {
            query = newQuery(
                id = id,
                options = options,
                initialValue = queryCache[key.id] as? QueryState<T> ?: newQueryState(key)
            ).also { queryStore[id] = it }
        }
        return QueryRef(
            key = key,
            query = query,
            options = options
        )
    }

    private fun <T> newQuery(
        id: UniqueId,
        options: QueryOptions,
        initialValue: QueryState<T>
    ): ManagedQuery<T> {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope))
        val event = MutableSharedFlow<QueryEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val state = MutableStateFlow(initialValue)
        val reducer = createQueryReducer<T>()
        val dispatch: QueryDispatch<T> = { action ->
            options.vvv(id) { "dispatching $action" }
            state.value = reducer(state.value, action)
        }
        val command = Channel<QueryCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = { seq ->
                scope.launch { gcFlow.emit { closeQuery<T>(id, seq) } }
            },
        ) {
            for (c in command) {
                options.vvv(id) { "next command $c" }
                c.handle(ctx = ManagedQueryContext(queryReceiver, options, state.value, dispatch))
            }
        }
        return ManagedQuery(
            id = id,
            options = options,
            scope = scope,
            dispatch = dispatch,
            actor = actor,
            event = event,
            state = state,
            command = command
        )
    }

    private fun <T> newQueryState(key: QueryKey<T>): QueryState<T> {
        val onPlaceholderData = key.onPlaceholderData() ?: return QueryState()
        val placeholderData = with(this) { onPlaceholderData() } ?: return QueryState()
        return QueryState(
            data = placeholderData,
            status = QueryStatus.Success,
            isPlaceholderData = true
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> closeQuery(id: UniqueId, seq: ActorSequenceNumber) {
        val query = queryStore[id] as? ManagedQuery<T> ?: return
        if (query.actor.seq == seq) {
            queryStore.remove(id)
            query.close()
            saveToCache(query)
        }
    }

    private fun <T> saveToCache(query: ManagedQuery<T>) {
        val lastValue = query.state.value
        val ttl = query.options.gcTime
        if (lastValue.isSuccess && !lastValue.isPlaceholderData && ttl.isPositive()) {
            queryCache.set(query.id, lastValue, ttl)
            query.options.vvv(query.id) { "cached(ttl=$ttl)" }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>
    ): InfiniteQueryRef<T, S> {
        val id = key.id
        val options = key.onConfigureOptions()?.invoke(defaultQueryOptions) ?: defaultQueryOptions
        var query = queryStore[id] as? ManagedQuery<QueryChunks<T, S>>
        if (query == null) {
            query = newInfiniteQuery(
                id = id,
                options = options,
                initialValue = queryCache[id] as? QueryState<QueryChunks<T, S>> ?: newInfiniteQueryState(key)
            ).also { queryStore[id] = it }
        }
        return InfiniteQueryRef(
            key = key,
            query = query,
            options = options
        )
    }

    private fun <T, S> newInfiniteQuery(
        id: UniqueId,
        options: QueryOptions,
        initialValue: QueryState<QueryChunks<T, S>>
    ): ManagedQuery<QueryChunks<T, S>> {
        return newQuery(
            id = id,
            options = options,
            initialValue = initialValue
        )
    }

    private fun <T, S> newInfiniteQueryState(
        key: InfiniteQueryKey<T, S>
    ): QueryState<QueryChunks<T, S>> {
        val onPlaceholderData = key.onPlaceholderData() ?: return QueryState()
        val placeholderData = with(this) { onPlaceholderData() } ?: return QueryState()
        return QueryState(
            data = placeholderData,
            status = QueryStatus.Success,
            isPlaceholderData = true
        )
    }

    override fun <T> prefetchQuery(key: QueryKey<T>) {
        val scope = CoroutineScope(policy.mainDispatcher)
        val query = getQuery(key).also { it.launchIn(scope) }
        coroutineScope.launch {
            val revision = query.state.value.revision
            val job = scope.launch { query.start() }
            try {
                withTimeout(query.options.prefetchWindowTime) {
                    query.state.first { it.revision != revision || !it.isStaled() }
                }
            } finally {
                job.cancel()
            }
        }
    }

    override fun <T, S> prefetchInfiniteQuery(key: InfiniteQueryKey<T, S>) {
        val scope = CoroutineScope(policy.mainDispatcher)
        val query = getInfiniteQuery(key).also { it.launchIn(scope) }
        coroutineScope.launch {
            val revision = query.state.value.revision
            val job = scope.launch { query.start() }
            try {
                withTimeout(query.options.prefetchWindowTime) {
                    query.state.first { it.revision != revision || !it.isStaled() }
                }
            } finally {
                job.cancel()
            }
        }
    }

    // ----- QueryMutableClient ----- //

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQueryData(id: QueryId<T>): T? {
        val query = queryStore[id] as? ManagedQuery<T>
        if (query != null) {
            return query.state.value.data
        }
        val state = queryCache[id] as? QueryState<T>
        if (state != null) {
            return state.data
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQueryData(id: InfiniteQueryId<T, S>): QueryChunks<T, S>? {
        val query = queryStore[id] as? ManagedQuery<QueryChunks<T, S>>
        if (query != null) {
            return query.state.value.data
        }
        val state = queryCache[id] as? QueryState<QueryChunks<T, S>>
        if (state != null) {
            return state.data
        }
        return null
    }

    override fun <T> updateQueryData(
        id: QueryId<T>,
        edit: T.() -> T
    ) {
        val data = getQueryData(id)
        if (data != null) {
            setQueryData(id, data.edit())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> setQueryData(id: QueryId<T>, data: T) {
        val query = queryStore[id] as? ManagedQuery<T>
        query?.forceUpdate(data)
        queryCache.swap(id) { copy(data = data) }
    }

    override fun <T, S> updateInfiniteQueryData(
        id: InfiniteQueryId<T, S>,
        edit: QueryChunks<T, S>.() -> QueryChunks<T, S>
    ) {
        val data = getInfiniteQueryData(id)
        if (!data.isNullOrEmpty()) {
            setInfiniteQueryData(id, data.edit())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T, S> setInfiniteQueryData(id: InfiniteQueryId<T, S>, data: QueryChunks<T, S>) {
        val query = queryStore[id] as? ManagedQuery<QueryChunks<T, S>>
        query?.forceUpdate(data)
        queryCache.swap(id) { copy(data = data) }
    }

    override fun invalidateQueries(filter: InvalidateQueriesFilter) {
        forEach(filter, ::invalidate)
    }

    override fun <U : UniqueId> invalidateQueriesBy(vararg ids: U) {
        require(ids.isNotEmpty())
        ids.forEach { id ->
            invalidate(id, QueryFilterType.Active)
            invalidate(id, QueryFilterType.Inactive)
        }
    }

    private fun invalidate(id: UniqueId, type: QueryFilterType) {
        when (type) {
            QueryFilterType.Active -> queryStore[id]?.invalidate()
            QueryFilterType.Inactive -> queryCache.swap(id) { copy(isInvalidated = true) }
        }
    }

    override fun removeQueries(filter: RemoveQueriesFilter) {
        forEach(filter, ::remove)
    }

    override fun <U : UniqueId> removeQueriesBy(vararg ids: U) {
        require(ids.isNotEmpty())
        ids.forEach { id ->
            remove(id, QueryFilterType.Active)
            remove(id, QueryFilterType.Inactive)
        }
    }

    private fun remove(id: UniqueId, type: QueryFilterType) {
        when (type) {
            QueryFilterType.Active -> queryStore.remove(id)?.close()
            QueryFilterType.Inactive -> queryCache.delete(id)
        }
    }

    override fun resumeQueries(filter: ResumeQueriesFilter) {
        // NOTE: resume targets only active queries.
        forEach(filter) { id, _ -> resume(id) }
    }

    override fun <U : UniqueId> resumeQueriesBy(vararg ids: U) {
        require(ids.isNotEmpty())
        ids.forEach { id -> resume(id) }
    }

    private fun resume(id: UniqueId) {
        queryStore[id]?.resume()
    }

    private fun forEach(
        filter: QueryFilter,
        action: (UniqueId, QueryFilterType) -> Unit
    ) {
        if (filter.type == null || filter.type == QueryFilterType.Active) {
            forEach(QueryFilterType.Active, filter.keys, filter.predicate) { id ->
                action(id, QueryFilterType.Active)
            }
        }
        if (filter.type == null || filter.type == QueryFilterType.Inactive) {
            forEach(QueryFilterType.Inactive, filter.keys, filter.predicate) { id ->
                action(id, QueryFilterType.Inactive)
            }
        }
    }

    private fun forEach(
        type: QueryFilterType,
        keys: Array<SurrogateKey>?,
        predicate: ((QueryModel<*>) -> Boolean)?,
        action: (UniqueId) -> Unit
    ) {
        val queryIds = when (type) {
            QueryFilterType.Active -> queryStore.keys
            QueryFilterType.Inactive -> queryCache.keys
        }
        queryIds.toSet()
            .asSequence()
            .filter { id ->
                if (keys.isNullOrEmpty()) true
                else keys.any { id.tags.contains(it) }
            }
            .filter { id ->
                if (predicate == null) true
                else isMatch(id, type, predicate)
            }
            .forEach(action)
    }

    private fun isMatch(
        id: UniqueId,
        type: QueryFilterType,
        predicate: ((QueryModel<*>) -> Boolean)
    ): Boolean {
        val model = when (type) {
            QueryFilterType.Active -> queryStore[id]?.state?.value
            QueryFilterType.Inactive -> queryCache[id]
        }
        return model?.let(predicate) ?: false
    }

    data class ManagedMutation<T> internal constructor(
        val id: UniqueId,
        val options: MutationOptions,
        val scope: CoroutineScope,
        val dispatch: MutationDispatch<T>,
        internal val actor: ActorBlockRunner,
        override val event: MutableSharedFlow<MutationEvent>,
        override val state: StateFlow<MutationState<T>>,
        override val command: SendChannel<MutationCommand<T>>
    ) : Mutation<T> {

        override fun launchIn(scope: CoroutineScope) {
            actor.launchIn(scope)
        }

        fun close() {
            scope.cancel()
            command.close()
        }

        fun ping(): Boolean {
            return event.tryEmit(MutationEvent.Ping)
        }
    }

    data class ManagedMutationContext<T>(
        override val receiver: MutationReceiver,
        override val options: MutationOptions,
        override val state: MutationState<T>,
        override val dispatch: MutationDispatch<T>,
        override val notifier: MutationNotifier
    ) : MutationCommand.Context<T>

    data class ManagedQuery<T> internal constructor(
        val id: UniqueId,
        val options: QueryOptions,
        val scope: CoroutineScope,
        val dispatch: QueryDispatch<T>,
        internal val actor: ActorBlockRunner,
        override val event: MutableSharedFlow<QueryEvent>,
        override val state: StateFlow<QueryState<T>>,
        override val command: SendChannel<QueryCommand<T>>
    ) : Query<T> {

        override fun launchIn(scope: CoroutineScope) {
            actor.launchIn(scope)
        }

        fun close() {
            scope.cancel()
            command.close()
        }

        fun invalidate() {
            dispatch(QueryAction.Invalidate)
            event.tryEmit(QueryEvent.Invalidate)
        }

        fun resume() {
            event.tryEmit(QueryEvent.Resume)
        }

        fun ping(): Boolean {
            return event.tryEmit(QueryEvent.Ping)
        }

        fun forceUpdate(data: T) {
            dispatch(QueryAction.ForceUpdate(data = data, dataUpdatedAt = epoch()))
        }
    }

    data class ManagedQueryContext<T>(
        override val receiver: QueryReceiver,
        override val options: QueryOptions,
        override val state: QueryState<T>,
        override val dispatch: QueryDispatch<T>
    ) : QueryCommand.Context<T>

    companion object {
        private fun newCoroutineContext(parent: CoroutineScope): CoroutineContext {
            return parent.coroutineContext + Job(parent.coroutineContext[Job])
        }
    }
}

/**
 * Policy for the [SwrCache].
 */
data class SwrCachePolicy(

    /**
     * [CoroutineScope] for coroutines executed on the [SwrCache].
     *
     * **Note:**
     * The [SwrCache] internals are not thread-safe.
     * Always use a scoped implementation such as [SwrCacheScope] or [kotlinx.coroutines.MainScope] with limited concurrency.
     */
    val coroutineScope: CoroutineScope,

    /**
     * [CoroutineDispatcher] for the main thread.
     *
     * **Note:**
     * Some garbage collection processes are safely synchronized with the caller using the main thread.
     */
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,

    /**
     * Default [MutationOptions] applied to [Mutation].
     */
    val mutationOptions: MutationOptions = MutationOptions,

    /**
     * Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
     */
    val mutationReceiver: MutationReceiver = MutationReceiver,

    /**
     * Management of active [Mutation] instances.
     */
    val mutationStore: MutableMap<UniqueId, SwrCache.ManagedMutation<*>> = mutableMapOf(),

    /**
     * Default [QueryOptions] applied to [Query].
     */
    val queryOptions: QueryOptions = QueryOptions,

    /**
     * Extension receiver for referencing external instances needed when executing [fetch][QueryKey.fetch].
     */
    val queryReceiver: QueryReceiver = QueryReceiver,

    /**
     * Management of active [Query] instances.
     */
    val queryStore: MutableMap<UniqueId, SwrCache.ManagedQuery<*>> = mutableMapOf(),

    /**
     * Management of cached data for inactive [Query] instances.
     */
    val queryCache: TimeBasedCache<UniqueId, QueryState<*>> = TimeBasedCache(DEFAULT_CAPACITY),

    /**
     * Receiving events of memory pressure.
     */
    val memoryPressure: MemoryPressure = MemoryPressure.Unsupported,

    /**
     * Receiving events of network connectivity.
     */
    val networkConnectivity: NetworkConnectivity = NetworkConnectivity.Unsupported,

    /**
     * The delay time to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeAfterDelay: Duration = 2.seconds,

    /**
     * The specified filter to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),

    /**
     * Receiving events of window visibility.
     */
    val windowVisibility: WindowVisibility = WindowVisibility.Unsupported,

    /**
     * The specified filter to resume queries after window visibility is refocused.
     *
     * **Note:**
     * This setting is only effective when [windowVisibility] is available.
     */
    val windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    ),

    /**
     * The chunk size for garbage collection. Default is [DEFAULT_GC_CHUNK_SIZE].
     */
    val gcChunkSize: Int = DEFAULT_GC_CHUNK_SIZE,

    /**
     * The interval for garbage collection. Default is [DEFAULT_GC_INTERVAL].
     */
    val gcInterval: Duration = DEFAULT_GC_INTERVAL
) {
    companion object {
        const val DEFAULT_CAPACITY = 50
        const val DEFAULT_GC_CHUNK_SIZE = 10
        val DEFAULT_GC_INTERVAL: Duration = 500.milliseconds
    }
}

/**
 * [CoroutineScope] with limited concurrency for [SwrCache].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwrCacheScope(parent: Job? = null) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        SupervisorJob(parent) +
            Dispatchers.Default.limitedParallelism(1) +
            CoroutineName("SwrCache")
}
