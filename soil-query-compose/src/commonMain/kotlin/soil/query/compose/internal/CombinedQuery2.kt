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


internal fun <T1, T2, R> combineQuery(
    query1: QueryRef<T1>,
    query2: QueryRef<T2>,
    transform: (T1, T2) -> R
): QueryRef<R> = CombinedQuery2(query1, query2, transform)

private class CombinedQuery2<T1, T2, R>(
    private val query1: QueryRef<T1>,
    private val query2: QueryRef<T2>,
    private val transform: (T1, T2) -> R
) : QueryRef<R> {

    override val id: QueryId<R> = QueryId("auto/${uuid()}")

    // FIXME: Switch to K2 mode when it becomes stable.
    private val _state: MutableStateFlow<QueryState<R>> = MutableStateFlow(
        value = merge(query1.state.value, query2.state.value)
    )
    override val state: StateFlow<QueryState<R>> = _state

    override suspend fun resume() {
        coroutineScope {
            val deferred1 = async { query1.resume() }
            val deferred2 = async { query2.resume() }
            awaitAll(deferred1, deferred2)
        }
    }

    override suspend fun invalidate() {
        coroutineScope {
            val deferred1 = async { query1.invalidate() }
            val deferred2 = async { query2.invalidate() }
            awaitAll(deferred1, deferred2)
        }
    }

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            combine(query1.state, query2.state, ::merge).collect { _state.value = it }
        }
    }

    private fun merge(state1: QueryState<T1>, state2: QueryState<T2>): QueryState<R> {
        return QueryState.merge(state1, state2, transform)
    }
}
