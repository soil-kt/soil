// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.combine
import soil.query.core.getOrThrow
import soil.query.core.isNone

/**
 * Merges multiple [QueryState] instances into a single [QueryState] instance.
 *
 * ## Merge specification details:
 * [QueryState.reply] is combined by invoking [transform] only when all [QueryState] instances have [Reply.Some].
 * [QueryState.status] is selected based on the following priority order:
 *   1. [QueryStatus.Failure]: If priorities are the same, the one with the oldest [QueryState.errorUpdatedAt] is selected.
 *   2. [QueryStatus.Pending]: If priorities are the same, the left-hand side is selected.
 *   3. [QueryStatus.Success]: If priorities are the same, the left-hand side is selected.
 *
 * For most other fields, the data associated with the [QueryState] selected by [QueryState.status] will be used as is.
 * However, [QueryState.replyUpdatedAt] is set to the initial value `0` only when [QueryState.reply] is [Reply.None].
 */
fun <T1, T2, R> QueryState.Companion.merge(
    state1: QueryState<T1>,
    state2: QueryState<T2>,
    transform: (T1, T2) -> R
): QueryState<R> {
    val reply = Reply.combine(state1.reply, state2.reply, transform)
    return merge(reply, pick(state1, state2))
}

/**
 * Merges multiple [QueryState] instances into a single [QueryState] instance.
 *
 * ## Merge specification details:
 * [QueryState.reply] is combined by invoking [transform] only when all [QueryState] instances have [Reply.Some].
 * [QueryState.status] is selected based on the following priority order:
 *   1. [QueryStatus.Failure]: If priorities are the same, the one with the oldest [QueryState.errorUpdatedAt] is selected.
 *   2. [QueryStatus.Pending]: If priorities are the same, the left-hand side is selected.
 *   3. [QueryStatus.Success]: If priorities are the same, the left-hand side is selected.
 *
 * For most other fields, the data associated with the [QueryState] selected by [QueryState.status] will be used as is.
 * However, [QueryState.replyUpdatedAt] is set to the initial value `0` only when [QueryState.reply] is [Reply.None].
 */
fun <T1, T2, T3, R> QueryState.Companion.merge(
    state1: QueryState<T1>,
    state2: QueryState<T2>,
    state3: QueryState<T3>,
    transform: (T1, T2, T3) -> R
): QueryState<R> {
    val reply = Reply.combine(state1.reply, state2.reply, state3.reply, transform)
    return merge(reply, pick(state1, state2, state3))
}

/**
 * Merges multiple [QueryState] instances into a single [QueryState] instance.
 *
 * ## Merge specification details:
 * [QueryState.reply] is combined by invoking [transform] only when all [QueryState] instances have [Reply.Some].
 * [QueryState.status] is selected based on the following priority order:
 *   1. [QueryStatus.Failure]: If priorities are the same, the one with the oldest [QueryState.errorUpdatedAt] is selected.
 *   2. [QueryStatus.Pending]: If priorities are the same, the left-hand side is selected.
 *   3. [QueryStatus.Success]: If priorities are the same, the left-hand side is selected.
 *
 * For most other fields, the data associated with the [QueryState] selected by [QueryState.status] will be used as is.
 * However, [QueryState.replyUpdatedAt] is set to the initial value `0` only when [QueryState.reply] is [Reply.None].
 */
fun <T, R> QueryState.Companion.merge(
    states: Array<QueryState<T>>,
    transform: (List<T>) -> R
): QueryState<R> {
    val values = states.filter { !it.reply.isNone }.map { it.reply.getOrThrow() }
    val reply = if (values.size == states.size) Reply.some(transform(values)) else Reply.none()
    return merge(reply, pick(*states))
}

private fun <R> merge(
    reply: Reply<R>,
    base: QueryState<*>
): QueryState<R> = QueryState(
    reply = reply,
    replyUpdatedAt = if (reply.isNone) 0 else base.replyUpdatedAt,
    error = base.error,
    errorUpdatedAt = base.errorUpdatedAt,
    staleAt = if (reply.isNone) 0 else base.staleAt,
    status = base.status,
    fetchStatus = base.fetchStatus,
    isInvalidated = base.isInvalidated
)

private fun pick(
    vararg states: QueryState<*>
): QueryState<*> = states.reduce { acc, st ->
    when {
        acc.isFailure && st.isFailure -> {
            if (acc.errorUpdatedAt <= st.errorUpdatedAt) acc else st
        }

        acc.isFailure -> acc
        st.isFailure -> st
        acc.isPending -> acc
        st.isPending -> st
        else -> acc
    }
}
