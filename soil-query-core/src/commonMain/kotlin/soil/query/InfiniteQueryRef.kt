// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import soil.query.core.toResultCallback
import kotlin.coroutines.cancellation.CancellationException

/**
 * A reference to an [Query] for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @property key Instance of a class implementing [InfiniteQueryKey].
 * @param query Transparently referenced [Query].
 * @constructor Creates an [InfiniteQueryRef].
 */
class InfiniteQueryRef<T, S>(
    val key: InfiniteQueryKey<T, S>,
    val options: QueryOptions,
    query: Query<QueryChunks<T, S>>
) : Query<QueryChunks<T, S>> by query {

    /**
     * Starts the [Query].
     *
     * This function must be invoked when a new mount point (subscriber) is added.
     */
    suspend fun start() {
        command.send(InfiniteQueryCommands.Connect(key))
        event.collect(::handleEvent)
    }

    /**
     * Prefetches the [Query].
     */
    suspend fun prefetch(): Boolean {
        val deferred = CompletableDeferred<QueryChunks<T, S>>()
        command.send(InfiniteQueryCommands.Connect(key, state.value.revision, deferred.toResultCallback()))
        return try {
            deferred.await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Invalidates the [Query].
     *
     * Calling this function will invalidate the retrieved data of the [Query],
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate() {
        command.send(InfiniteQueryCommands.Invalidate(key, state.value.revision))
    }

    /**
     * Resumes the [Query].
     */
    private suspend fun resume() {
        command.send(InfiniteQueryCommands.Connect(key, state.value.revision))
    }

    /**
     * Fetches data for the [InfiniteQueryKey] using the value of [param].
     */
    suspend fun loadMore(param: S) {
        command.send(InfiniteQueryCommands.LoadMore(key, param))
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
