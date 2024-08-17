// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.core.DataModel
import soil.query.core.Reply
import soil.query.core.epoch

/**
 * Promise-like data structure that represents the state of a value that is being loaded.
 *
 * Currently, this interface is intended for temporary use as a migration to queries.
 * Useful when combining the `query.compose.runtime` package with other asynchronous processing.
 *
 * @param T The type of the value that has been loaded.
 */
@Stable
sealed class Loadable<out T> : DataModel<T> {

    override fun isAwaited(): Boolean = this == Pending

    /**
     * Represents the state of a value that is being loaded.
     */
    @Immutable
    data object Pending : Loadable<Nothing>() {
        override val reply: Reply<Nothing> = Reply.None
        override val replyUpdatedAt: Long = 0
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
    }

    /**
     * Represents the state of a value that has been loaded.
     */
    @Immutable
    data class Fulfilled<T>(
        val data: T
    ) : Loadable<T>() {
        override val reply: Reply<T> = Reply.some(data)
        override val replyUpdatedAt: Long = epoch()
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
    }

    /**
     * Represents the state of a value that has been rejected.
     */
    @Immutable
    data class Rejected(
        override val error: Throwable
    ) : Loadable<Nothing>() {
        override val reply: Reply<Nothing> = Reply.None
        override val replyUpdatedAt: Long = 0
        override val errorUpdatedAt: Long = epoch()
    }
}
