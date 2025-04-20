// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Immutable
import soil.query.core.DataModel
import soil.query.core.Reply

@Immutable
private sealed class Symbol<out T> : DataModel<T> {
    data object None : Symbol<Nothing>() {
        override val reply: Reply<Nothing> = Reply.None
        override val replyUpdatedAt: Long = 0
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
        override fun isAwaited(): Boolean = false
    }

    data object Pending : Symbol<Nothing>() {
        override val reply: Reply<Nothing> = Reply.None
        override val replyUpdatedAt: Long = 0
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
        override fun isAwaited(): Boolean = true
    }

}

/**
 * Returns a [DataModel] that represents no data.
 */
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "orNone()",
        "soil.plant.compose.reacty.orNone"
    ),
    level = DeprecationLevel.WARNING
)
fun <T> DataModel<T>?.orNone(): DataModel<T> = this ?: Symbol.None

/**
 * Returns a [DataModel] that represents pending data.
 */
@Deprecated(
    message = "This implementation is deprecated. Please use the new implementation from soil-reacty module instead.",
    replaceWith = ReplaceWith(
        "orPending()",
        "soil.plant.compose.reacty.orPending"
    ),
    level = DeprecationLevel.WARNING
)
fun <T> DataModel<T>?.orPending(): DataModel<T> = this ?: Symbol.Pending
