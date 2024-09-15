// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import soil.query.QueryRef
import soil.query.QueryState

/**
 * A mechanism to finely adjust the behavior of the query on a component basis in Composable functions.
 *
 * If you want to customize, please create a class implementing [QueryStrategy].
 * For example, this is useful when you want to switch your implementation to `collectAsStateWithLifecycle`.
 *
 * @see CachingStrategy
 */
@Stable
interface QueryStrategy {

    @Composable
    fun <T> collectAsState(query: QueryRef<T>): QueryState<T>

    companion object
}

/**
 * The default built-in strategy for Query built into the library.
 */
val QueryStrategy.Companion.Default: QueryStrategy
    get() = DefaultQueryStrategy

private object DefaultQueryStrategy : QueryStrategy {
    @Composable
    override fun <T> collectAsState(query: QueryRef<T>): QueryState<T> {
        val state by query.state.collectAsState()
        LaunchedEffect(query.id) {
            query.resume()
        }
        return state
    }
}
