// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import soil.query.core.DataModel
import soil.query.core.Reply

/**
 * Returns a [DataModel] that represents no data.
 */
fun <T> DataModel<T>?.orNone(): DataModel<T> = this ?: None

private object None : DataModel<Nothing> {
    override val reply: Reply<Nothing> = Reply.None
    override val replyUpdatedAt: Long = 0
    override val error: Throwable? = null
    override val errorUpdatedAt: Long = 0
    override fun isAwaited(): Boolean = false
}

/**
 * Returns a [DataModel] that represents pending data.
 */
fun <T> DataModel<T>?.orPending(): DataModel<T> = this ?: Pending

private object Pending : DataModel<Nothing> {
    override val reply: Reply<Nothing> = Reply.None
    override val replyUpdatedAt: Long = 0
    override val error: Throwable? = null
    override val errorUpdatedAt: Long = 0
    override fun isAwaited(): Boolean = true
}
