// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.QueryClient
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.compose.QueryConfig

internal fun <T> newQuery(
    key: QueryKey<T>,
    config: QueryConfig,
    client: QueryClient,
    scope: CoroutineScope
): QueryRef<T> = Query(key, config, client, scope)

private class Query<T>(
    key: QueryKey<T>,
    config: QueryConfig,
    client: QueryClient,
    private val scope: CoroutineScope,
) : QueryRef<T>, RememberObserver {

    private val query: QueryRef<T> = client.getQuery(key, config.marker)
    private val optimize: (QueryState<T>) -> QueryState<T> = config.optimizer::omit

    override val id: QueryId<T> = query.id

    // FIXME: Switch to K2 mode when it becomes stable.
    private val _state: MutableStateFlow<QueryState<T>> = MutableStateFlow(
        value = optimize(query.state.value)
    )
    override val state: StateFlow<QueryState<T>> = _state

    override fun close() = query.close()

    override suspend fun resume() = query.resume()

    override suspend fun invalidate() = query.invalidate()

    override suspend fun join() = query.join()

    // ----- RememberObserver -----//
    private var job: Job? = null

    override fun onAbandoned() = stop()

    override fun onForgotten() = stop()

    override fun onRemembered() = start()

    private fun start() {
        job = scope.launch {
            query.state.collect { _state.value = optimize(it) }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        close()
    }
}
