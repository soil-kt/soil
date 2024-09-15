// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.core.getOrElse
import soil.query.core.isNone
import soil.query.core.map
import soil.query.emptyChunks

/**
 * A mapper that converts [QueryState] to [InfiniteQueryObject].
 */
interface InfiniteQueryObjectMapper {

    /**
     * Converts the given [QueryState] to [InfiniteQueryObject].
     *
     * @param query The query reference.
     * @param select A function that selects the object from the chunks.
     * @return The converted object.
     */
    fun <T, S, U> QueryState<QueryChunks<T, S>>.toObject(
        query: InfiniteQueryRef<T, S>,
        select: (chunks: QueryChunks<T, S>) -> U
    ): InfiniteQueryObject<U, S>

    companion object
}

/**
 * The default [InfiniteQueryObjectMapper].
 */
val InfiniteQueryObjectMapper.Companion.Default: InfiniteQueryObjectMapper
    get() = DefaultInfiniteQueryObjectMapper

private object DefaultInfiniteQueryObjectMapper : InfiniteQueryObjectMapper {
    override fun <T, S, U> QueryState<QueryChunks<T, S>>.toObject(
        query: InfiniteQueryRef<T, S>,
        select: (chunks: QueryChunks<T, S>) -> U
    ): InfiniteQueryObject<U, S> = when (status) {
        QueryStatus.Pending -> InfiniteQueryLoadingObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = null
        )

        QueryStatus.Success -> InfiniteQuerySuccessObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = query.nextParam(reply.getOrElse { emptyChunks() })
        )

        QueryStatus.Failure -> if (reply.isNone) {
            InfiniteQueryLoadingErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = null
            )
        } else {
            InfiniteQueryRefreshErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = query.nextParam(reply.getOrElse { emptyChunks() })
            )
        }
    }
}
