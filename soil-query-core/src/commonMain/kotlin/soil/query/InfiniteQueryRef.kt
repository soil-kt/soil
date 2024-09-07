// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.core.Actor
import soil.query.core.Marker
import soil.query.core.awaitOrNull


/**
 * A reference to an Query for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
interface InfiniteQueryRef<T, S> : Actor {

    /**
     * The [InfiniteQueryKey] for the Query.
     */
    val key: InfiniteQueryKey<T, S>

    /**
     * The QueryOptions configured for the query.
     */
    val options: QueryOptions

    /**
     * The Marker specified in [QueryClient.getInfiniteQuery].
     */
    val marker: Marker

    /**
     * [State Flow][StateFlow] to receive the current state of the query.
     */
    val state: StateFlow<QueryState<QueryChunks<T, S>>>

    /**
     * Sends a [QueryCommand] to the Actor.
     */
    suspend fun send(command: InfiniteQueryCommand<T, S>)

    /**
     * Resumes the Query.
     */
    suspend fun resume() {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.Connect(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    /**
     * Fetches data for the [InfiniteQueryKey] using the value of [param].
     */
    suspend fun loadMore(param: S) {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.LoadMore(key, param, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    /**
     * Invalidates the Query.
     *
     * Calling this function will invalidate the retrieved data of the Query,
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate() {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        send(InfiniteQueryCommands.Invalidate(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }
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
    query: Query<QueryChunks<T, S>>
): InfiniteQueryRef<T, S> {
    return InfiniteQueryRefImpl(key, marker, query)
}

private class InfiniteQueryRefImpl<T, S>(
    override val key: InfiniteQueryKey<T, S>,
    override val marker: Marker,
    private val query: Query<QueryChunks<T, S>>
) : InfiniteQueryRef<T, S> {

    override val options: QueryOptions
        get() = query.options

    override val state: StateFlow<QueryState<QueryChunks<T, S>>>
        get() = query.state

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            query.launchIn(this)
            query.event.collect(::handleEvent)
        }
    }

    override suspend fun send(command: InfiniteQueryCommand<T, S>) {
        query.command.send(command)
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
