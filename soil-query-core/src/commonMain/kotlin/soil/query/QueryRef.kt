// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import soil.query.core.toResultCallback
import kotlin.coroutines.cancellation.CancellationException

/**
 * A reference to an [Query] for [QueryKey].
 *
 * @param T Type of data to retrieve.
 * @property key Instance of a class implementing [QueryKey].
 * @param query Transparently referenced [Query].
 * @constructor Creates an [QueryRef]
 */
class QueryRef<T>(
    val key: QueryKey<T>,
    val options: QueryOptions,
    query: Query<T>
) : Query<T> by query {

    /**
     * Starts the [Query].
     *
     * This function must be invoked when a new mount point (subscriber) is added.
     */
    suspend fun start() {
        command.send(QueryCommands.Connect(key))
        event.collect(::handleEvent)
    }

    /**
     * Prefetches the [Query].
     */
    suspend fun prefetch(): Boolean {
        val deferred = CompletableDeferred<T>()
        command.send(QueryCommands.Connect(key, state.value.revision, deferred.toResultCallback()))
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
        command.send(QueryCommands.Invalidate(key, state.value.revision))
    }

    /**
     * Resumes the [Query].
     */
    internal suspend fun resume() {
        command.send(QueryCommands.Connect(key, state.value.revision))
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
