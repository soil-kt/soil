// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus
import soil.query.core.Reply
import soil.query.core.getOrNull
import soil.query.core.getOrThrow


/**
 * A QueryObject represents [QueryModel]s interface for fetching data.
 *
 * @param T Type of data to retrieve.
 */
@Stable
sealed interface QueryObject<out T> : QueryModel<T> {

    /**
     * The return value from the data source. (Backward compatibility with QueryModel)
     */
    val data: T?

    /**
     * Refreshes the data.
     */
    val refresh: suspend () -> Unit
}

/**
 * A QueryIdleObject represents the initial loading state of the [QueryObject].
 *
 * @param T Type of data to retrieve.
 */
@Immutable
data class QueryLoadingObject<T>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Pending
    override val data: T? get() = reply.getOrNull()
}

/**
 * A QueryLoadingErrorObject represents the initial loading error state of the [QueryObject].
 *
 * @param T Type of data to retrieve.
 */
@Immutable
data class QueryLoadingErrorObject<T>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Failure
    override val data: T? get() = reply.getOrNull()
}

/**
 * A QuerySuccessObject represents the successful state of the [QueryObject].
 *
 * @param T Type of data to retrieve.
 */
@Immutable
data class QuerySuccessObject<T>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Success
    override val data: T get() = reply.getOrThrow()
}

/**
 * A QueryRefreshErrorObject represents the refresh error state of the [QueryObject].
 *
 * This state is used when the data is successfully retrieved once, but an error occurs during the refresh.
 *
 * @param T Type of data to retrieve.
 * @constructor Creates a [QueryRefreshErrorObject].
 */
@Immutable
data class QueryRefreshErrorObject<T>(
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Failure
    override val data: T get() = reply.getOrThrow()
}
