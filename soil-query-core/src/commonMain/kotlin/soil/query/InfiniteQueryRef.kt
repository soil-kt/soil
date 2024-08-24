// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor
import soil.query.core.awaitOrNull

/**
 * A reference to an Query for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
interface InfiniteQueryRef<T, S> : Actor {

    val key: InfiniteQueryKey<T, S>
    val options: QueryOptions
    val state: StateFlow<QueryState<QueryChunks<T, S>>>

    /**
     * Sends a [QueryCommand] to the Actor.
     */
    suspend fun send(command: InfiniteQueryCommand<T, S>)
}

/**
 * Invalidates the Query.
 *
 * Calling this function will invalidate the retrieved data of the Query,
 * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
 */
suspend fun <T, S> InfiniteQueryRef<T, S>.invalidate() {
    val deferred = CompletableDeferred<QueryChunks<T, S>>()
    send(InfiniteQueryCommands.Invalidate(key, state.value.revision, deferred::completeWith))
    deferred.awaitOrNull()
}

/**
 * Resumes the Query.
 */
suspend fun <T, S> InfiniteQueryRef<T, S>.resume() {
    val deferred = CompletableDeferred<QueryChunks<T, S>>()
    send(InfiniteQueryCommands.Connect(key, state.value.revision, deferred::completeWith))
    deferred.awaitOrNull()
}

/**
 * Fetches data for the [InfiniteQueryKey] using the value of [param].
 */
suspend fun <T, S> InfiniteQueryRef<T, S>.loadMore(param: S) {
    val deferred = CompletableDeferred<QueryChunks<T, S>>()
    send(InfiniteQueryCommands.LoadMore(key, param, deferred::completeWith))
    deferred.awaitOrNull()
}
