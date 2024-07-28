// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus
import soil.query.internal.epoch

/**
 * Promise-like data structure that represents the state of a value that is being loaded.
 *
 * Currently, this interface is intended for temporary use as a migration to queries.
 * Useful when combining the `query.compose.runtime` package with other asynchronous processing.
 *
 * @param T The type of the value that has been loaded.
 */
@Stable
sealed class Loadable<out T> : QueryModel<T> {

    /**
     * Represents the state of a value that is being loaded.
     */
    @Immutable
    data object Pending : Loadable<Nothing>() {
        override val data: Nothing get() = error("Pending")
        override val dataUpdatedAt: Long = 0
        override val dataStaleAt: Long = 0
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
        override val status: QueryStatus = QueryStatus.Pending
        override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Fetching
        override val isInvalidated: Boolean = false
        override val isPlaceholderData: Boolean = false
    }

    /**
     * Represents the state of a value that has been loaded.
     */
    @Immutable
    data class Fulfilled<T>(
        override val data: T
    ) : Loadable<T>() {
        override val dataUpdatedAt: Long = epoch()
        override val dataStaleAt: Long = Long.MAX_VALUE
        override val error: Throwable? = null
        override val errorUpdatedAt: Long = 0
        override val status: QueryStatus = QueryStatus.Success
        override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle
        override val isInvalidated: Boolean = false
        override val isPlaceholderData: Boolean = false
    }

    /**
     * Represents the state of a value that has been rejected.
     */
    @Immutable
    data class Rejected(
        override val error: Throwable
    ) : Loadable<Nothing>() {
        override val data: Nothing get() = error("Rejected")
        override val dataUpdatedAt: Long = 0
        override val dataStaleAt: Long = 0
        override val errorUpdatedAt: Long = epoch()
        override val status: QueryStatus = QueryStatus.Failure
        override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle
        override val isInvalidated: Boolean = false
        override val isPlaceholderData: Boolean = false
    }
}
