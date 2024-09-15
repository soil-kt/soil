// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import soil.query.QueryId
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.core.uuid
import soil.query.merge


internal fun <T, R> combineQuery(
    queries: Array<QueryRef<T>>,
    transform: (List<T>) -> R
): QueryRef<R> = CombinedQueryN(queries, transform)

private class CombinedQueryN<T, R>(
    private val queries: Array<QueryRef<T>>,
    private val transform: (List<T>) -> R
) : QueryRef<R> {

    override val id: QueryId<R> = QueryId("auto/${uuid()}")

    // FIXME: Switch to K2 mode when it becomes stable.
    private val _state: MutableStateFlow<QueryState<R>> = MutableStateFlow(
        value = merge(queries.map { it.state.value }.toTypedArray())
    )
    override val state: StateFlow<QueryState<R>> = _state

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

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            combine(queries.map { it.state }, ::merge).collect { _state.value = it }
        }
    }

    private fun merge(states: Array<QueryState<T>>): QueryState<R> {
        return QueryState.merge(states, transform)
    }
}
