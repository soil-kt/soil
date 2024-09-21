// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryState
import soil.query.compose.InfiniteQueryConfig

internal fun <T, S> newInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    config: InfiniteQueryConfig,
    client: QueryClient,
    scope: CoroutineScope
): InfiniteQueryRef<T, S> = InfiniteQuery(key, config, client, scope)

private class InfiniteQuery<T, S>(
    key: InfiniteQueryKey<T, S>,
    config: InfiniteQueryConfig,
    client: QueryClient,
    private val scope: CoroutineScope
) : InfiniteQueryRef<T, S>, RememberObserver {

    private val query: InfiniteQueryRef<T, S> = client.getInfiniteQuery(key, config.marker)
    private val optimize: (QueryState<QueryChunks<T, S>>) -> QueryState<QueryChunks<T, S>> = config.optimizer::omit

    override val id: InfiniteQueryId<T, S> = query.id

    private val _state: MutableStateFlow<QueryState<QueryChunks<T, S>>> = MutableStateFlow(
        value = optimize(query.state.value)
    )
    override val state: StateFlow<QueryState<QueryChunks<T, S>>> = _state

    override fun nextParam(data: QueryChunks<T, S>): S? = query.nextParam(data)

    override suspend fun resume() = query.resume()

    override suspend fun loadMore(param: S) = query.loadMore(param)

    override suspend fun invalidate() = query.invalidate()

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            query.state.collect { _state.value = optimize(it) }
        }
    }

    // ----- RememberObserver -----//
    private var jobs: List<Job>? = null

    override fun onAbandoned() = stop()

    override fun onForgotten() = stop()

    override fun onRemembered() {
        stop()
        start()
    }

    private fun start() {
        val job1 = query.launchIn(scope)
        val job2 = launchIn(scope)
        jobs = listOf(job1, job2)
    }

    private fun stop() {
        jobs?.forEach { it.cancel() }
        jobs = null
    }
}
