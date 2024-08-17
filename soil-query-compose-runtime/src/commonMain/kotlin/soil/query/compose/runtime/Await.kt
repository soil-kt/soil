// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import soil.query.core.DataModel
import soil.query.core.Reply
import soil.query.core.combine
import soil.query.core.uuid

/**
 * Await for a [DataModel] to be fulfilled.
 *
 * The content will be displayed when the query is fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T Type of data to retrieve.
 * @param state The [DataModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the query is fulfilled.
 */
@Composable
inline fun <T> Await(
    state: DataModel<T>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data: T) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    when (val reply = state.reply) {
        is Reply.Some -> content(reply.value)
        is Reply.None -> Unit
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
 * Await for two [DataModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param state1 The first [DataModel] to await.
 * @param state2 The second [DataModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2> Await(
    state1: DataModel<T1>,
    state2: DataModel<T2>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    when (val reply = Reply.combine(state1.reply, state2.reply, ::Pair)) {
        is Reply.Some -> content(reply.value.first, reply.value.second)
        is Reply.None -> Unit
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
 * Await for three [DataModel] to be fulfilled.
 *
 * The content will be displayed when the queries are fulfilled.
 * The await will be managed by the [AwaitHost].
 *
 * @param T1 Type of data to retrieve.
 * @param T2 Type of data to retrieve.
 * @param T3 Type of data to retrieve.
 * @param state1 The first [DataModel] to await.
 * @param state2 The second [DataModel] to await.
 * @param state3 The third [DataModel] to await.
 * @param key The key to identify the await.
 * @param host The [AwaitHost] to manage the await. By default, it uses the [LocalAwaitHost].
 * @param content The content to display when the queries are fulfilled.
 */
@Composable
inline fun <T1, T2, T3> Await(
    state1: DataModel<T1>,
    state2: DataModel<T2>,
    state3: DataModel<T3>,
    key: Any? = null,
    host: AwaitHost = LocalAwaitHost.current,
    crossinline content: @Composable (data1: T1, data2: T2, data3: T3) -> Unit
) {
    val id = remember(key) { key ?: uuid() }
    when (val reply = Reply.combine(state1.reply, state2.reply, state3.reply, ::Triple)) {
        is Reply.Some -> content(reply.value.first, reply.value.second, reply.value.third)
        is Reply.None -> Unit
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
