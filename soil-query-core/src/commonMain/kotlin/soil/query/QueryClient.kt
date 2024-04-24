// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.UniqueId

interface QueryClient {

    val defaultQueryOptions: QueryOptions

    fun <T> getQuery(key: QueryKey<T>): QueryRef<T>

    fun <T, S> getInfiniteQuery(key: InfiniteQueryKey<T, S>): InfiniteQueryRef<T, S>

    fun <T> prefetchQuery(key: QueryKey<T>)

    fun <T, S> prefetchInfiniteQuery(key: InfiniteQueryKey<T, S>)
}

interface QueryReadonlyClient {
    fun <T> getQueryData(id: QueryId<T>): T?

    fun <T, S> getInfiniteQueryData(id: InfiniteQueryId<T, S>): QueryChunks<T, S>?
}

interface QueryMutableClient : QueryReadonlyClient {

    fun <T> updateQueryData(
        id: QueryId<T>,
        edit: T.() -> T
    )

    fun <T, S> updateInfiniteQueryData(
        id: InfiniteQueryId<T, S>,
        edit: QueryChunks<T, S>.() -> QueryChunks<T, S>
    )

    fun invalidateQueries(filter: InvalidateQueriesFilter)

    fun <U : UniqueId> invalidateQueriesBy(vararg ids: U)

    fun removeQueries(filter: RemoveQueriesFilter)

    fun <U : UniqueId> removeQueriesBy(vararg ids: U)

    fun resumeQueries(filter: ResumeQueriesFilter)

    fun <U : UniqueId> resumeQueriesBy(vararg ids: U)
}

typealias QueryPlaceholderData<T> = QueryReadonlyClient.() -> T?
typealias QueryEffect = QueryMutableClient.() -> Unit

typealias QueryRecoverData<T> = (error: Throwable) -> T
