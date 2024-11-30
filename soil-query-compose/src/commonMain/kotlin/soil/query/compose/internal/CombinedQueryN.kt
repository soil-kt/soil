// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import soil.query.QueryClient
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.compose.QueryConfig
import soil.query.core.uuid
import soil.query.merge

internal fun <T, R> newCombinedQuery(
    keys: List<QueryKey<T>>,
    transform: (List<T>) -> R,
    config: QueryConfig,
    client: QueryClient,
    scope: CoroutineScope
): QueryRef<R> = CombinedQueryN(keys, transform, config, client, scope)

private class CombinedQueryN<T, R>(
    keys: List<QueryKey<T>>,
    private val transform: (List<T>) -> R,
    config: QueryConfig,
    client: QueryClient,
    private val scope: CoroutineScope
) : QueryRef<R>, RememberObserver {

    private val queries: List<QueryRef<T>> = keys.map { key -> client.getQuery(key, config.marker) }
    private val optimize: (QueryState<R>) -> QueryState<R> = config.optimizer::omit

    override val id: QueryId<R> = QueryId("auto/${uuid()}")

    // FIXME: Switch to K2 mode when it becomes stable.
    private val _state: MutableStateFlow<QueryState<R>> = MutableStateFlow(
        value = merge(queries.map { it.state.value }.toTypedArray())
    )
    override val state: StateFlow<QueryState<R>> = _state

    override fun close() {
        queries.forEach { it.close() }
    }

    override suspend fun resume() {
        coroutineScope {
            queries.map { query -> async { query.resume() } }.awaitAll()
        }
    }

    override suspend fun invalidate() {
        coroutineScope {
            queries.map { query -> async { query.invalidate() } }.awaitAll()
        }
    }

    override suspend fun join() {
        coroutineScope {
            queries.map { query -> launch { query.join() } }.joinAll()
        }
    }

    private fun merge(states: Array<QueryState<T>>): QueryState<R> {
        return optimize(QueryState.merge(states, transform))
    }

    // ----- RememberObserver -----//
    private var job: Job? = null

    override fun onAbandoned() = stop()

    override fun onForgotten() = stop()

    override fun onRemembered() = start()

    private fun start() {
        job = scope.launch {
            combine(queries.map { it.state }, ::merge).collect { _state.value = it }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        close()
    }
}
