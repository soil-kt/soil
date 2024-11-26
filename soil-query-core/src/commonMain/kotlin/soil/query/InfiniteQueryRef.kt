// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.InstanceId
import soil.query.core.Marker
import soil.query.core.awaitOrNull
import soil.query.core.uuid


/**
 * A reference to an Query for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
interface InfiniteQueryRef<T, S> : AutoCloseable {

    /**
     * A unique identifier used for managing [InfiniteQueryKey].
     */
    val id: InfiniteQueryId<T, S>

    /**
     * [State Flow][StateFlow] to receive the current state of the query.
     */
    val state: StateFlow<QueryState<QueryChunks<T, S>>>

    /**
     * Function returning the parameter for additional fetching.
     *
     * @param data The data to be used to determine the next parameter.
     * @return `null` if there is no more data to fetch.
     * @see loadMore
     */
    fun nextParam(data: QueryChunks<T, S>): S?

    /**
     * Resumes the Query.
     */
    suspend fun resume()

    /**
     * Fetches data for the [InfiniteQueryKey] using the value of [param].
     *
     * @see nextParam
     */
    suspend fun loadMore(param: S)

    /**
     * Invalidates the Query.
     *
     * Calling this function will invalidate the retrieved data of the Query,
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate()

    /**
     * Joins the Query.
     *
     * Calling this function will start receiving [events][QueryEvent].
     */
    suspend fun join()
}

/**
 * Creates a new [InfiniteQueryRef] instance.
 *
 * @param key The [InfiniteQueryKey] for the Query.
 * @param marker The Marker specified in [QueryClient.getInfiniteQuery].
 * @param query The Query to create a reference.
 */
fun <T, S> InfiniteQueryRef(
    key: InfiniteQueryKey<T, S>,
    marker: Marker,
    query: Query<QueryChunks<T, S>>,
    iid: InstanceId = uuid()
): InfiniteQueryRef<T, S> {
    return InfiniteQueryRefImpl(key, marker, query, iid)
}

private class InfiniteQueryRefImpl<T, S>(
    private val key: InfiniteQueryKey<T, S>,
    private val marker: Marker,
    private val query: Query<QueryChunks<T, S>>,
    private val iid: InstanceId
) : InfiniteQueryRef<T, S> {

    init {
        query.attach(iid)
    }

    override val id: InfiniteQueryId<T, S>
        get() = key.id

    override val state: StateFlow<QueryState<QueryChunks<T, S>>>
        get() = query.state

    override fun close() {
        query.detach(iid)
    }

    override fun nextParam(data: QueryChunks<T, S>): S? {
        return key.loadMoreParam(data)
    }

    override suspend fun resume() {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.Connect(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    override suspend fun loadMore(param: S) {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.LoadMore(key, param, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    override suspend fun invalidate() {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.Invalidate(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    override suspend fun join() {
        query.event.collect(::handleEvent)
    }

    private suspend fun send(command: InfiniteQueryCommand<T, S>) {
        query.command.send(command)
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
