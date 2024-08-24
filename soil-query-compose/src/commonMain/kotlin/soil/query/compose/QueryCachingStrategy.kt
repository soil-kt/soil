// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.StateFlow
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.UniqueId
import soil.query.core.isNone

/**
 * A mechanism to finely adjust the behavior of the query results on a component basis in Composable functions.
 *
 * In addition to the default behavior provided by Stale-While-Revalidate, two experimental strategies are now available:
 *
 * 1. Cache-First:
 *  This strategy avoids requesting data re-fetch as long as valid cached data is available.
 *  It prioritizes using the cached data over network requests.
 *
 * 2. Network-First:
 *  This strategy maintains the initial loading state until data is re-fetched, regardless of the presence of valid cached data.
 *  This ensures that the most up-to-date data is always displayed.
 *
 * If you want to customize further, please create a class implementing [QueryCachingStrategy].
 * However, as this is an experimental API, the interface may change significantly in future versions.
 *
 * In future updates, we plan to provide additional options for more granular control over the behavior at the component level.
 *
 * Background:
 * During in-app development, there are scenarios where returning cached data first can lead to issues.
 * For example, if the externally updated data state is not accurately reflected on the screen, inconsistencies can occur.
 * This is particularly problematic in processes that automatically redirect to other screens based on the data state.
 *
 * On the other hand, there are situations where data re-fetching should be suppressed to minimize data traffic.
 * In such cases, setting a long staleTime in QueryOptions is not sufficient, as specific conditions for reducing data traffic may persist.
 */
@Stable
interface QueryCachingStrategy {
    @Composable
    fun <T> collectAsState(query: QueryRef<T>): QueryState<T>

    @Composable
    fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>>

    companion object Default : QueryCachingStrategy by StaleWhileRevalidate {

        @Suppress("FunctionName")
        @ExperimentalSoilQueryApi
        fun CacheFirst(): QueryCachingStrategy = CacheFirst

        @Suppress("FunctionName")
        @ExperimentalSoilQueryApi
        fun NetworkFirst(): QueryCachingStrategy = NetworkFirst
    }
}

@Stable
private object StaleWhileRevalidate : QueryCachingStrategy {
    @Composable
    override fun <T> collectAsState(query: QueryRef<T>): QueryState<T> {
        return collectAsState(key = query.key.id, flow = query.state, resume = query::resume)
    }

    @Composable
    override fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>> {
        return collectAsState(key = query.key.id, flow = query.state, resume = query::resume)
    }

    @Composable
    private inline fun <T> collectAsState(
        key: UniqueId,
        flow: StateFlow<QueryState<T>>,
        crossinline resume: suspend () -> Unit
    ): QueryState<T> {
        val state by flow.collectAsState()
        LaunchedEffect(key) {
            resume()
        }
        return state
    }
}


@Stable
private object CacheFirst : QueryCachingStrategy {
    @Composable
    override fun <T> collectAsState(query: QueryRef<T>): QueryState<T> {
        return collectAsState(query.key.id, query.state, query::resume)
    }

    @Composable
    override fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>> {
        return collectAsState(query.key.id, query.state, query::resume)
    }

    @Composable
    private inline fun <T> collectAsState(
        key: UniqueId,
        flow: StateFlow<QueryState<T>>,
        crossinline resume: suspend () -> Unit
    ): QueryState<T> {
        val state by flow.collectAsState()
        LaunchedEffect(key) {
            val currentValue = flow.value
            if (currentValue.reply.isNone || currentValue.isInvalidated) {
                resume()
            }
        }
        return state
    }
}

@Stable
private object NetworkFirst : QueryCachingStrategy {
    @Composable
    override fun <T> collectAsState(query: QueryRef<T>): QueryState<T> {
        return collectAsState(query.key.id, query.state, query::resume)
    }

    @Composable
    override fun <T, S> collectAsState(query: InfiniteQueryRef<T, S>): QueryState<QueryChunks<T, S>> {
        return collectAsState(query.key.id, query.state, query::resume)
    }

    @Composable
    private inline fun <T> collectAsState(
        key: UniqueId,
        flow: StateFlow<QueryState<T>>,
        crossinline resume: suspend () -> Unit
    ): QueryState<T> {
        var resumed by rememberSaveable(key) { mutableStateOf(false) }
        val initialValue = if (resumed) flow.value else QueryState.initial()
        val state = produceState(initialValue, key) {
            resume()
            resumed = true
            flow.collect { value = it }
        }
        return state.value
    }
}
