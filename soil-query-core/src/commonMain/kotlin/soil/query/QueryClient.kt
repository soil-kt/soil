// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.Job
import soil.query.core.Marker
import soil.query.core.UniqueId

/**
 * A Query client, which allows you to make queries actor and handle [QueryKey] and [InfiniteQueryKey].
 */
interface QueryClient {

    /**
     * The default query options.
     */
    val defaultQueryOptions: QueryOptions

    /**
     * Gets the [QueryRef] by the specified [QueryKey].
     */
    fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker = Marker.None
    ): QueryRef<T>

    /**
     * Gets the [InfiniteQueryRef] by the specified [InfiniteQueryKey].
     */
    fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker = Marker.None
    ): InfiniteQueryRef<T, S>

    /**
     * Prefetches the query by the specified [QueryKey].
     *
     * **Note:**
     * Prefetch is executed within a [kotlinx.coroutines.CoroutineScope] associated with the instance of [QueryClient].
     * After data retrieval, subscription is automatically unsubscribed, hence the caching period depends on [QueryOptions].
     */
    fun <T> prefetchQuery(
        key: QueryKey<T>,
        marker: Marker = Marker.None
    ): Job

    /**
     * Prefetches the infinite query by the specified [InfiniteQueryKey].
     *
     * **Note:**
     * Prefetch is executed within a [kotlinx.coroutines.CoroutineScope] associated with the instance of [QueryClient].
     * After data retrieval, subscription is automatically unsubscribed, hence the caching period depends on [QueryOptions].
     */
    fun <T, S> prefetchInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker = Marker.None
    ): Job
}

/**
 * Interface for directly accessing retrieved [Query] data by [QueryClient].
 *
 * [QueryInitialData] is designed to calculate initial data from other [Query].
 * This is useful when the type included in the list on the overview screen matches the type of content on the detailed screen.
 */
interface QueryReadonlyClient {

    /**
     * Retrieves data of the [QueryKey] associated with the [id].
     */
    fun <T> getQueryData(id: QueryId<T>): T?

    /**
     * Retrieves data of the [InfiniteQueryKey] associated with the [id].
     */
    fun <T, S> getInfiniteQueryData(id: InfiniteQueryId<T, S>): QueryChunks<T, S>?
}

/**
 * Interface for causing side effects on [Query] under the control of [QueryClient].
 *
 * [QueryEffect] is designed to allow side effects such as updating, deleting, and revalidating queries.
 * It is useful for handling [MutationKey.onQueryUpdate] after executing [Mutation] that affects [Query] data.
 */
interface QueryMutableClient : QueryReadonlyClient {

    /**
     * Updates the data of the [QueryKey] associated with the [id].
     */
    fun <T> updateQueryData(
        id: QueryId<T>,
        edit: T.() -> T
    )

    /**
     * Updates the data of the [InfiniteQueryKey] associated with the [id].
     */
    fun <T, S> updateInfiniteQueryData(
        id: InfiniteQueryId<T, S>,
        edit: QueryChunks<T, S>.() -> QueryChunks<T, S>
    )

    /**
     * Invalidates the queries by the specified [InvalidateQueriesFilter].
     */
    fun invalidateQueries(filter: InvalidateQueriesFilter)

    /**
     * Invalidates the queries by the specified [UniqueId].
     */
    fun <U : UniqueId> invalidateQueriesBy(vararg ids: U)

    /**
     * Removes the queries by the specified [RemoveQueriesFilter].
     *
     * **Note:**
     * Queries will be removed from [QueryClient], but [QueryRef] instances on the subscriber side will remain until they are dereferenced.
     * Also, the [kotlinx.coroutines.CoroutineScope] associated with the [kotlinx.coroutines.Job] will be canceled at the time of removal.
     */
    fun removeQueries(filter: RemoveQueriesFilter)

    /**
     * Removes the queries by the specified [UniqueId].
     */
    fun <U : UniqueId> removeQueriesBy(vararg ids: U)

    /**
     * Resumes the queries by the specified [ResumeQueriesFilter].
     */
    fun resumeQueries(filter: ResumeQueriesFilter)

    /**
     * Resumes the queries by the specified [UniqueId].
     */
    fun <U : UniqueId> resumeQueriesBy(vararg ids: U)
}

typealias QueryInitialData<T> = QueryReadonlyClient.() -> T?
typealias QueryEffect = QueryMutableClient.() -> Unit

typealias QueryContentEquals<T> = (oldData: T, newData: T) -> Boolean
typealias QueryContentCacheable<T> = (currentData: T) -> Boolean
typealias QueryRecoverData<T> = (error: Throwable) -> T
typealias QueryOptionsOverride = (QueryOptions) -> QueryOptions
typealias QueryCallback<T> = (Result<T>) -> Unit
