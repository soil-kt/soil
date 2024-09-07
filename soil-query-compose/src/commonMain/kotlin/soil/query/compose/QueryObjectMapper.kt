// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.QueryRef
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.core.isNone
import soil.query.core.map

/**
 * A mapper that converts [QueryState] to [QueryObject].
 */
interface QueryObjectMapper {

    /**
     * Converts the given [QueryState] to [QueryObject].
     *
     * @param query The query reference.
     * @param select A function that selects the object from the reply.
     * @return The converted object.
     */
    fun <T, U> QueryState<T>.toObject(
        query: QueryRef<T>,
        select: (T) -> U
    ): QueryObject<U>

    companion object
}

/**
 * The default [QueryObjectMapper].
 */
val QueryObjectMapper.Companion.Default: QueryObjectMapper
    get() = DefaultQueryObjectMapper

private object DefaultQueryObjectMapper : QueryObjectMapper {
    override fun <T, U> QueryState<T>.toObject(
        query: QueryRef<T>,
        select: (T) -> U
    ): QueryObject<U> = when (status) {
        QueryStatus.Pending -> QueryLoadingObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate
        )

        QueryStatus.Success -> QuerySuccessObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate
        )

        QueryStatus.Failure -> if (reply.isNone) {
            QueryLoadingErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate
            )
        } else {
            QueryRefreshErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                staleAt = staleAt,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate
            )
        }
    }
}
