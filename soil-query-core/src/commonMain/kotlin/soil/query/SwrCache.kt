// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import soil.query.core.ActorBlockRunner
import soil.query.core.ActorSequenceNumber
import soil.query.core.BatchScheduler
import soil.query.core.ErrorRecord
import soil.query.core.Marker
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import soil.query.core.SurrogateKey
import soil.query.core.TimeBasedCache
import soil.query.core.UniqueId
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent
import soil.query.core.epoch
import soil.query.core.getOrNull
import soil.query.core.vvv
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of the [SwrClient] interface.
 *
 * [Query] internally manages two categories:
 * - Active: When there is one or more references or within the [QueryOptions.keepAliveTime] period
 * - Inactive: When there are no references and past the [QueryOptions.keepAliveTime] period
 *
 * [Query] in the Active state does not disappear from memory unless one of the following conditions is met:
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

    @Suppress("unused")
    constructor(coroutineScope: CoroutineScope) : this(SwrCachePolicy(coroutineScope))

    private val mutationReceiver = policy.mutationReceiver
    private val mutationStore: MutableMap<UniqueId, ManagedMutation<*>> = mutableMapOf()
    private val queryReceiver = policy.queryReceiver
    private val queryStore: MutableMap<UniqueId, ManagedQuery<*>> = mutableMapOf()
    private val queryCache: QueryCache = policy.queryCache
    private val batchScheduler: BatchScheduler = policy.batchScheduler
    private val coroutineScope: CoroutineScope = CoroutineScope(
        context = newCoroutineContext(policy.coroutineScope)
    )

    private var mountedIds: Set<String> = emptySet()
    private var mountedScope: CoroutineScope? = null

    init {
        batchScheduler.start(coroutineScope)
    }

    /**
     * Releases data in memory based on the specified [level].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun gc(level: MemoryPressureLevel = MemoryPressureLevel.Low) {
        when (level) {
            MemoryPressureLevel.Low -> evictCache()
            MemoryPressureLevel.High -> clearCache()
        }
    }

    private fun evictCache() {
        queryCache.evict()
    }

    private fun clearCache() {
        queryCache.clear()
    }

    // ----- SwrClient ----- //

    override val defaultMutationOptions: MutationOptions = policy.mutationOptions
    override val defaultQueryOptions: QueryOptions = policy.queryOptions

    override val errorRelay: Flow<ErrorRecord>
        get() = policy.errorRelay?.receiveAsFlow() ?: error("policy.errorRelay is not configured :(")

    override fun perform(sideEffects: QueryEffect): Job {
        return coroutineScope.launch {
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
            .collect { level ->
                withContext(policy.mainDispatcher) {
                    gc(level)
                }
            }
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
        key: MutationKey<T, S>,
        marker: Marker
    ): MutationRef<T, S> {
        val id = key.id
        var mutation = mutationStore[id] as? ManagedMutation<T>
        if (mutation == null) {
            mutation = newMutation(
                id = id,
                options = key.configureOptions(defaultMutationOptions),
                initialValue = MutationState<T>()
            ).also { mutationStore[id] = it }
        }
        return SwrMutation(
            key = key,
            marker = marker,
            mutation = mutation
        )
    }

    private fun <T> newMutation(
        id: UniqueId,
        options: MutationOptions,
        initialValue: MutationState<T>
    ): ManagedMutation<T> {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope))
        val state = MutableStateFlow(initialValue)
        val reducer = createMutationReducer<T>()
        val dispatch: MutationDispatch<T> = { action ->
            options.vvv(id) { "dispatching $action" }
            state.value = reducer(state.value, action)
        }
        val notifier = MutationNotifier { effect -> perform(effect) }
        val relay: MutationErrorRelay? = policy.errorRelay?.let { it::send }
        val command = Channel<MutationCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = { seq ->
                scope.launch { batchScheduler.post { closeMutation<T>(id, seq) } }
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
                        notifier = notifier,
                        relay = relay
                    )
                )
            }
        }
        return ManagedMutation(
            id = id,
            options = options,
            scope = scope,
            actor = actor,
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
    override fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker
    ): QueryRef<T> {
        val id = key.id
        var query = queryStore[id] as? ManagedQuery<T>
        if (query == null) {
            query = newQuery(
                id = id,
                options = key.configureOptions(defaultQueryOptions),
                initialValue = queryCache[key.id] as? QueryState<T> ?: newQueryState(key)
            ).also { queryStore[id] = it }
        }
        return SwrQuery(
            key = key,
            marker = marker,
            query = query
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
        val relay: QueryErrorRelay? = policy.errorRelay?.let { it::send }
        val command = Channel<QueryCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = { seq ->
                scope.launch { batchScheduler.post { closeQuery<T>(id, seq) } }
            },
        ) {
            for (c in command) {
                options.vvv(id) { "next command $c" }
                c.handle(
                    ctx = ManagedQueryContext(
                        receiver = queryReceiver,
                        options = options,
                        state = state.value,
                        dispatch = dispatch,
                        relay = relay
                    )
                )
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
        val onInitialData = key.onInitialData() ?: return QueryState()
        val initialData = with(this) { onInitialData() } ?: return QueryState()
        return QueryState.success(data = initialData, dataUpdatedAt = 0)
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
        if (lastValue.isSuccess && ttl.isPositive()) {
            queryCache.set(query.id, lastValue, ttl)
            query.options.vvv(query.id) { "cached(ttl=$ttl)" }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): InfiniteQueryRef<T, S> {
        val id = key.id
        var query = queryStore[id] as? ManagedQuery<QueryChunks<T, S>>
        if (query == null) {
            query = newInfiniteQuery(
                id = id,
                options = key.configureOptions(defaultQueryOptions),
                initialValue = queryCache[id] as? QueryState<QueryChunks<T, S>> ?: QueryState()
            ).also { queryStore[id] = it }
        }
        return SwrInfiniteQuery(
            key = key,
            marker = marker,
            query = query
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

    override fun <T> prefetchQuery(
        key: QueryKey<T>,
        marker: Marker
    ): Job {
        val scope = CoroutineScope(policy.mainDispatcher)
        val query = getQuery(key, marker).also { it.launchIn(scope) }
        return coroutineScope.launch {
            try {
                val options = key.configureOptions(defaultQueryOptions)
                withTimeoutOrNull(options.prefetchWindowTime) {
                    query.resume()
                }
            } finally {
                scope.cancel()
            }
        }
    }

    override fun <T, S> prefetchInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): Job {
        val scope = CoroutineScope(policy.mainDispatcher)
        val query = getInfiniteQuery(key, marker).also { it.launchIn(scope) }
        return coroutineScope.launch {
            try {
                val options = key.configureOptions(defaultQueryOptions)
                withTimeoutOrNull(options.prefetchWindowTime) {
                    query.resume()
                }
            } finally {
                scope.cancel()
            }
        }
    }

    // ----- QueryMutableClient ----- //

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQueryData(id: QueryId<T>): T? {
        val query = queryStore[id] as? ManagedQuery<T>
        if (query != null) {
            return query.state.value.reply.getOrNull()
        }
        val state = queryCache[id] as? QueryState<T>
        if (state != null) {
            return state.reply.getOrNull()
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQueryData(id: InfiniteQueryId<T, S>): QueryChunks<T, S>? {
        val query = queryStore[id] as? ManagedQuery<QueryChunks<T, S>>
        if (query != null) {
            return query.state.value.reply.getOrNull()
        }
        val state = queryCache[id] as? QueryState<QueryChunks<T, S>>
        if (state != null) {
            return state.reply.getOrNull()
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
        queryCache.swap(id) {
            this as QueryState<T>
            patch(data)
        }
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
        queryCache.swap(id) {
            this as QueryState<QueryChunks<T, S>>
            patch(data)
        }
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

    internal class ManagedMutation<T>(
        val id: UniqueId,
        val options: MutationOptions,
        val scope: CoroutineScope,
        internal val actor: ActorBlockRunner,
        override val state: StateFlow<MutationState<T>>,
        override val command: SendChannel<MutationCommand<T>>
    ) : Mutation<T> {

        override fun launchIn(scope: CoroutineScope): Job {
            return actor.launchIn(scope)
        }

        fun close() {
            scope.cancel()
            command.close()
        }
    }

    internal class ManagedMutationContext<T>(
        override val receiver: MutationReceiver,
        override val options: MutationOptions,
        override val state: MutationState<T>,
        override val dispatch: MutationDispatch<T>,
        override val notifier: MutationNotifier,
        override val relay: MutationErrorRelay?
    ) : MutationCommand.Context<T>

    internal class ManagedQuery<T>(
        val id: UniqueId,
        val options: QueryOptions,
        val scope: CoroutineScope,
        val dispatch: QueryDispatch<T>,
        internal val actor: ActorBlockRunner,
        override val event: MutableSharedFlow<QueryEvent>,
        override val state: StateFlow<QueryState<T>>,
        override val command: SendChannel<QueryCommand<T>>
    ) : Query<T> {

        override fun launchIn(scope: CoroutineScope): Job {
            return actor.launchIn(scope)
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

        fun forceUpdate(data: T) {
            dispatch(QueryAction.ForceUpdate(data = data, dataUpdatedAt = epoch()))
        }
    }

    internal class ManagedQueryContext<T>(
        override val receiver: QueryReceiver,
        override val options: QueryOptions,
        override val state: QueryState<T>,
        override val dispatch: QueryDispatch<T>,
        override val relay: QueryErrorRelay?
    ) : QueryCommand.Context<T>

    companion object {
        private fun newCoroutineContext(parent: CoroutineScope): CoroutineContext {
            return parent.coroutineContext + Job(parent.coroutineContext[Job])
        }
    }
}

private fun <T, S> MutationKey<T, S>.configureOptions(defaultOptions: MutationOptions): MutationOptions {
    return onConfigureOptions()?.invoke(defaultOptions) ?: defaultOptions
}

private fun <T> QueryKey<T>.configureOptions(defaultOptions: QueryOptions): QueryOptions {
    return onConfigureOptions()?.invoke(defaultOptions) ?: defaultOptions
}

private fun <T, S> InfiniteQueryKey<T, S>.configureOptions(defaultOptions: QueryOptions): QueryOptions {
    return onConfigureOptions()?.invoke(defaultOptions) ?: defaultOptions
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
