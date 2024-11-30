// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.EffectContext
import soil.query.core.EffectPropertyKey
import soil.query.core.UniqueId

typealias QueryEffect = QueryEffectClient.() -> Unit

/**
 * Interface for causing side effects on [Query] under the control of [QueryClient].
 *
 * [QueryEffect] is designed to allow side effects such as updating, deleting, and revalidating queries.
 * It is useful for handling [MutationKey.onQueryUpdate] after executing [Mutation] that affects [Query] data.
 */
interface QueryEffectClient : QueryReadonlyClient {

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

/**
 * Executes the specified [block] with the [QueryEffectClient].
 */
fun EffectContext.withQuery(block: QueryEffect) = with(queryClient, block)

/**
 * Gets the [QueryEffectClient] from the [EffectContext].
 */
val EffectContext.queryClient: QueryEffectClient
    get() = get(queryEffectClientPropertyKey)

internal val queryEffectClientPropertyKey = EffectPropertyKey<QueryEffectClient>(
    errorDescription = """
        QueryEffectClient is not available.
        This might indicate an issue within the library.
        Please report this to the library maintainers.
    """.trimIndent()
)
