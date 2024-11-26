// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import soil.query.annotation.InternalSoilQueryApi
import soil.query.core.Actor
import soil.query.core.ActorBlockRunner
import soil.query.core.BatchScheduler
import soil.query.core.ErrorRelay
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.SurrogateKey
import soil.query.core.UniqueId
import soil.query.core.epoch
import soil.query.core.getOrNull
import soil.query.core.vvv
import kotlin.coroutines.CoroutineContext

@InternalSoilQueryApi
abstract class SwrCacheInternal : MutationClient, QueryClient, QueryMutableClient {

    protected abstract val coroutineScope: CoroutineScope
    protected abstract val mainDispatcher: CoroutineDispatcher
    protected abstract val mutationOptions: MutationOptions
    protected abstract val mutationReceiver: MutationReceiver
    protected abstract val mutationStore: MutableMap<UniqueId, ManagedMutation<*>>
    protected abstract val queryOptions: QueryOptions
    protected abstract val queryReceiver: QueryReceiver
    protected abstract val queryStore: MutableMap<UniqueId, ManagedQuery<*>>
    protected abstract val queryCache: QueryCache
    protected abstract val batchScheduler: BatchScheduler
    protected abstract val errorRelaySource: ErrorRelay?

    protected fun launch(effectBlock: QueryEffect): Job {
        return coroutineScope.launch {
            with(this@SwrCacheInternal) { effectBlock() }
        }
    }

    protected fun resetQueries() {
        val queryStoreCopy = queryStore.toMap()
        queryStore.clear()
        queryCache.clear()
        queryStoreCopy.values.forEach { it.cancel() }
    }

    protected fun resetMutations() {
        val mutationStoreCopy = mutationStore.toMap()
        mutationStore.clear()
        mutationStoreCopy.values.forEach { it.cancel() }
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
                options = key.onConfigureOptions()?.invoke(mutationOptions) ?: mutationOptions,
                initialValue = MutationState<T>()
            ).also { mutationStore[id] = it }
        }
        return MutationRef(
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
        val notifier = MutationNotifier { effect -> launch(effect) }
        val relay: MutationErrorRelay? = errorRelaySource?.let { it::send }
        val command = Channel<MutationCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = {
                scope.launch { batchScheduler.post { deactivateMutation<T>(id) } }
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
            state = state,
            command = command,
            scope = scope,
            actor = actor
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deactivateMutation(id: UniqueId) {
        val mutation = mutationStore[id] as? ManagedMutation<T> ?: return
        if (mutation.hasAttachedInstances()) {
            mutation.options.vvv(mutation.id) { "deactivate aborted: instances attached" }
            return
        }
        mutationStore.remove(id)
        mutation.cancel()
        mutation.options.vvv(mutation.id) { "deactivated" }
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
                options = key.onConfigureOptions()?.invoke(queryOptions) ?: queryOptions,
                initialValue = queryCache[key.id] as? QueryState<T> ?: newQueryState(key),
                contentCacheable = key.contentCacheable
            ).also { queryStore[id] = it }
        }
        return QueryRef(
            key = key,
            marker = marker,
            query = query
        )
    }

    private fun <T> newQuery(
        id: UniqueId,
        options: QueryOptions,
        initialValue: QueryState<T>,
        contentCacheable: QueryContentCacheable<T>?
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
        val relay: QueryErrorRelay? = errorRelaySource?.let { it::send }
        val command = Channel<QueryCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = {
                scope.launch { batchScheduler.post { deactivateQuery<T>(id) } }
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
            event = event,
            state = state,
            command = command,
            scope = scope,
            actor = actor,
            dispatch = dispatch,
            cacheable = contentCacheable
        )
    }

    private fun <T> newQueryState(key: QueryKey<T>): QueryState<T> {
        val onInitialData = key.onInitialData() ?: return QueryState()
        val initialData = with(this) { onInitialData() } ?: return QueryState()
        return QueryState.success(data = initialData, dataUpdatedAt = 0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deactivateQuery(id: UniqueId) {
        val query = queryStore[id] as? ManagedQuery<T> ?: return
        if (query.hasAttachedInstances()) {
            query.options.vvv(query.id) { "deactivate aborted: instances attached" }
            return
        }
        queryStore.remove(id)
        query.cancel()
        query.options.vvv(query.id) { "deactivated" }
        saveToCache(query)
    }

    private fun <T> saveToCache(query: ManagedQuery<T>) {
        val lastValue = query.state.value
        val ttl = query.options.gcTime
        val saveable = lastValue.isSuccess && ttl.isPositive()
        if (saveable && query.isCacheable(lastValue.reply)) {
            queryCache.set(query.id, lastValue, ttl)
            query.options.vvv(query.id) { "cached(ttl=$ttl)" }
        } else {
            queryCache.delete(query.id)
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
                options = key.onConfigureOptions()?.invoke(queryOptions) ?: queryOptions,
                initialValue = queryCache[id] as? QueryState<QueryChunks<T, S>> ?: QueryState(),
                contentCacheable = key.contentCacheable
            ).also { queryStore[id] = it }
        }
        return InfiniteQueryRef(
            key = key,
            marker = marker,
            query = query
        )
    }

    private fun <T, S> newInfiniteQuery(
        id: UniqueId,
        options: QueryOptions,
        initialValue: QueryState<QueryChunks<T, S>>,
        contentCacheable: QueryContentCacheable<QueryChunks<T, S>>?
    ): ManagedQuery<QueryChunks<T, S>> {
        return newQuery(
            id = id,
            options = options,
            initialValue = initialValue,
            contentCacheable = contentCacheable
        )
    }

    override fun <T> prefetchQuery(
        key: QueryKey<T>,
        marker: Marker
    ): Job {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope) + mainDispatcher)
        return scope.launch {
            getQuery(key, marker).use { query ->
                val options = key.onConfigureOptions()?.invoke(queryOptions) ?: queryOptions
                withTimeoutOrNull(options.prefetchWindowTime) {
                    query.resume()
                }
            }
        }
    }

    override fun <T, S> prefetchInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): Job {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope) + mainDispatcher)
        return scope.launch {
            getInfiniteQuery(key, marker).use { query ->
                val options = key.onConfigureOptions()?.invoke(queryOptions) ?: queryOptions
                withTimeoutOrNull(options.prefetchWindowTime) {
                    query.resume()
                }
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
            QueryFilterType.Active -> queryStore.remove(id)?.cancel()
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

    protected fun forEach(
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

    @InternalSoilQueryApi
    class ManagedMutation<T> internal constructor(
        val id: UniqueId,
        val options: MutationOptions,
        override val state: StateFlow<MutationState<T>>,
        override val command: SendChannel<MutationCommand<T>>,
        private val scope: CoroutineScope,
        private val actor: ActorBlockRunner,
    ) : Mutation<T>, Actor by actor {

        fun cancel() {
            scope.cancel()
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

    @InternalSoilQueryApi
    class ManagedQuery<T> internal constructor(
        val id: UniqueId,
        val options: QueryOptions,
        override val event: MutableSharedFlow<QueryEvent>,
        override val state: StateFlow<QueryState<T>>,
        override val command: SendChannel<QueryCommand<T>>,
        private val scope: CoroutineScope,
        private val actor: ActorBlockRunner,
        private val dispatch: QueryDispatch<T>,
        private val cacheable: QueryContentCacheable<T>?,
    ) : Query<T>, Actor by actor {

        fun cancel() {
            scope.cancel()
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

        fun isCacheable(reply: Reply<T>): Boolean {
            return when (reply) {
                is Reply.None -> false
                is Reply.Some<T> -> {
                    cacheable?.invoke(reply.value) ?: true
                }
            }
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
        internal fun newCoroutineContext(parent: CoroutineScope): CoroutineContext {
            return parent.coroutineContext + Job(parent.coroutineContext[Job])
        }
    }
}
