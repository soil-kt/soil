// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.compose.InfiniteQueryLoadingErrorObject
import soil.query.compose.InfiniteQueryLoadingObject
import soil.query.compose.InfiniteQueryRefreshErrorObject
import soil.query.compose.InfiniteQuerySuccessObject
import soil.query.compose.QueryLoadingErrorObject
import soil.query.compose.QueryLoadingObject
import soil.query.compose.QueryRefreshErrorObject
import soil.query.compose.QuerySuccessObject
import soil.query.internal.uuid

/**
 * Await for a [QueryModel] to be fulfilled.
 *
 * The content will be displayed when the query is fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T Type of data to retrieve.
 * @param state The [QueryModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the query is fulfilled.
 */
@Composable
inline fun <T> Await(
    state: QueryModel<T>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data: T) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    AwaitHandler(state) { data ->
        content(data)
    }
    LaunchedEffect(id, state) {
        host[id] = state.isAwaited()
    }
    DisposableEffect(id) {
        onDispose {
            host.remove(id)
        }
    }
}

/**
 * Await for two [QueryModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param state1 The first [QueryModel] to await.
 * @param state2 The second [QueryModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2> Await(
    state1: QueryModel<T1>,
    state2: QueryModel<T2>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    AwaitHandler(state1) { d1 ->
        AwaitHandler(state2) { d2 ->
            content(d1, d2)
        }
    }
    LaunchedEffect(id, state1, state2) {
        host[id] = listOf(state1, state2).any { it.isAwaited() }
    }
    DisposableEffect(id) {
        onDispose {
            host.remove(id)
        }
    }
}

/**
 * Await for three [QueryModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param T3 Type of data to retrieve.
 * @param state1 The first [QueryModel] to await.
 * @param state2 The second [QueryModel] to await.
 * @param state3 The third [QueryModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2, T3> Await(
    state1: QueryModel<T1>,
    state2: QueryModel<T2>,
    state3: QueryModel<T3>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2, data3: T3) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    AwaitHandler(state1) { d1 ->
        AwaitHandler(state2) { d2 ->
            AwaitHandler(state3) { d3 ->
                content(d1, d2, d3)
            }
        }
    }
    LaunchedEffect(id, state1, state2, state3) {
        host[id] = listOf(state1, state2, state3).any { it.isAwaited() }
    }
    DisposableEffect(id) {
        onDispose {
            host.remove(id)
        }
    }
}

/**
 * Await for four [QueryModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param T3 Type of data to retrieve.
 * @param T4 Type of data to retrieve.
 * @param state1 The first [QueryModel] to await.
 * @param state2 The second [QueryModel] to await.
 * @param state3 The third [QueryModel] to await.
 * @param state4 The fourth [QueryModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2, T3, T4> Await(
    state1: QueryModel<T1>,
    state2: QueryModel<T2>,
    state3: QueryModel<T3>,
    state4: QueryModel<T4>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2, data3: T3, data4: T4) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    AwaitHandler(state1) { d1 ->
        AwaitHandler(state2) { d2 ->
            AwaitHandler(state3) { d3 ->
                AwaitHandler(state4) { d4 ->
                    content(d1, d2, d3, d4)
                }
            }
        }
    }
    LaunchedEffect(id, state1, state2, state3, state4) {
        host[id] = listOf(state1, state2, state3, state4).any { it.isAwaited() }
    }
    DisposableEffect(id) {
        onDispose {
            host.remove(id)
        }
    }
}

/**
 * Await for five [QueryModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param T3 Type of data to retrieve.
 * @param T4 Type of data to retrieve.
 * @param T5 Type of data to retrieve.
 * @param state1 The first [QueryModel] to await.
 * @param state2 The second [QueryModel] to await.
 * @param state3 The third [QueryModel] to await.
 * @param state4 The fourth [QueryModel] to await.
 * @param state5 The fifth [QueryModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2, T3, T4, T5> Await(
    state1: QueryModel<T1>,
    state2: QueryModel<T2>,
    state3: QueryModel<T3>,
    state4: QueryModel<T4>,
    state5: QueryModel<T5>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2, data3: T3, data4: T4, data5: T5) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    AwaitHandler(state1) { d1 ->
        AwaitHandler(state2) { d2 ->
            AwaitHandler(state3) { d3 ->
                AwaitHandler(state4) { d4 ->
                    AwaitHandler(state5) { d5 ->
                        content(d1, d2, d3, d4, d5)
                    }
                }
            }
        }
    }
    LaunchedEffect(id, state1, state2, state3, state4) {
        host[id] = listOf(state1, state2, state3, state4).any { it.isAwaited() }
    }
    DisposableEffect(id) {
        onDispose {
            host.remove(id)
        }
    }
}

/**
 * Await for [QueryModel] to be fulfilled.
 *
 * This function is part of the [Await].
 * It is used to handle the [QueryModel] state and display the content when the query is fulfilled.
 *
 * @param T Type of data to retrieve.
 * @param state The [QueryModel] to await.
 * @param content The content to display when the query is fulfilled.
 */
@Composable
fun <T> AwaitHandler(
    state: QueryModel<T>,
    content: @Composable (data: T) -> Unit
) {
    when (state) {
        is QuerySuccessObject<T> -> content(state.data)
        is QueryRefreshErrorObject<T> -> content(state.data)
        is QueryLoadingErrorObject<T>,
        is QueryLoadingObject<T> -> Unit

        is InfiniteQuerySuccessObject<T, *> -> content(state.data)
        is InfiniteQueryRefreshErrorObject<T, *> -> content(state.data)
        is InfiniteQueryLoadingErrorObject<T, *>,
        is InfiniteQueryLoadingObject<T, *> -> Unit

        else -> {
            if (state.isSuccess || (state.isFailure && state.dataUpdatedAt > 0)) {
                content(state.data!!)
            }
        }
    }
}

/**
 * Returns true if the [QueryModel] is awaited.
 */
fun QueryModel<*>.isAwaited(): Boolean {
    return isPending
        || (isFailure && fetchStatus == QueryFetchStatus.Fetching)
        || (isInvalidated && fetchStatus == QueryFetchStatus.Fetching)
}
