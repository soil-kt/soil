// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

interface DataModel<out T> {

    /**
     * The return value from the data source.
     */
    val reply: Reply<T>

    /**
     * The timestamp when the data was updated.
     */
    val replyUpdatedAt: Long

    /**
     * The error that occurred.
     */
    val error: Throwable?

    /**
     * The timestamp when the error occurred.
     */
    val errorUpdatedAt: Long

    /**
     * Returns true if the [DataModel] is awaited.
     */
    fun isAwaited(): Boolean
}
