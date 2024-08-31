// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryState

/**
 * A mechanism to finely adjust the behavior of the infinite-query on a component basis in Composable functions.
 *
 * If you want to customize, please create a class implementing [InfiniteQueryStrategy].
 * For example, this is useful when you want to switch your implementation to `collectAsStateWithLifecycle`.
 *
 * @see CachingStrategy
 */
@Stable
interface InfiniteQueryStrategy {

    @Composable
    fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>>

    companion object Default : InfiniteQueryStrategy {

        @Composable
        override fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>> {
            val state by query.state.collectAsState()
            LaunchedEffect(query.key.id) {
                query.resume()
            }
            return state
        }
    }
}
