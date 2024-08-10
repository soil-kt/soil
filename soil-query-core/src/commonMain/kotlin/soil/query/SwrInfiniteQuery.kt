// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.core.toResultCallback

internal class SwrInfiniteQuery<T, S>(
    override val key: InfiniteQueryKey<T, S>,
    override val options: QueryOptions,
    private val query: Query<QueryChunks<T, S>>
) : InfiniteQueryRef<T, S> {

    override val state: StateFlow<QueryState<QueryChunks<T, S>>>
        get() = query.state

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            query.launchIn(this)
            query.event.collect(::handleEvent)
        }
    }

    override suspend fun send(command: QueryCommand<QueryChunks<T, S>>) {
        query.command.send(command)
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}

/**
 * Prefetches the Query.
 */
internal suspend fun <T, S> InfiniteQueryRef<T, S>.prefetch(): Boolean {
    val deferred = CompletableDeferred<QueryChunks<T, S>>()
    send(InfiniteQueryCommands.Connect(key, state.value.revision, deferred.toResultCallback()))
    return try {
        deferred.await()
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        false
    }
}
