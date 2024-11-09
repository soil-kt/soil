// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.UniqueId

/**
 * [InfiniteQueryKey] for managing [Query] associated with [id].
 *
 * The difference from [QueryKey] is that it's an interface for infinite fetching data using a retrieval method known as "infinite scroll."
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
interface InfiniteQueryKey<T, S> {

    /**
     * A unique identifier used for managing [InfiniteQueryKey].
     */
    val id: InfiniteQueryId<T, S>

    /**
     * Suspending function to retrieve data.
     *
     * @receiver QueryReceiver You can use a custom QueryReceiver within the fetch function.
     */
    val fetch: suspend QueryReceiver.(param: S) -> T

    /**
     * Function returning the initial parameter.
     */
    val initialParam: () -> S

    /**
     * Function returning the parameter for additional fetching.
     *
     * `chunks` contains the retrieved data.
     */
    val loadMoreParam: (chunks: QueryChunks<T, S>) -> S?

    /**
     * Function to compare the content of the data.
     *
     * This function is used to determine whether the data is identical to the previous data via [InfiniteQueryCommand].
     * If the data is considered the same, only [QueryState.staleAt] is updated.
     * This can be useful when strict update management is needed, such as when special comparison is necessary,
     * although it is generally not that important.
     *
     * @see QueryKey.contentEquals
     */
    val contentEquals: QueryContentEquals<QueryChunks<T, S>>? get() = null

    /**
     * Function to determine if the data should be cached.
     *
     * This function evaluates the provided data to decide whether it is suitable for caching.
     * If the function returns `true`, the data is considered cacheable; otherwise, it is not.
     * This can be useful in cases where caching is conditional based on specific data attributes
     * or properties that indicate the content should be stored temporarily.
     *
     * ```kotlin
     * override val contentCacheable: QueryContentCacheable<SomeType> = { data -> data.isNotEmpty() }
     * ```
     */
    val contentCacheable: QueryContentCacheable<QueryChunks<T, S>>? get() = null

    /**
     * Function to configure the [QueryOptions].
     *
     * If unspecified, the default value of [SwrCachePolicy] is used.
     *
     * @see QueryKey.onConfigureOptions
     */
    fun onConfigureOptions(): QueryOptionsOverride? = null

    /**
     * Function to convert specific exceptions as data.
     *
     * Depending on the type of exception that occurred during data retrieval, it is possible to recover it as normal data.
     *
     * @see QueryKey.onRecoverData
     */
    fun onRecoverData(): QueryRecoverData<QueryChunks<T, S>>? = null
}

/**
 * Unique identifier for [InfiniteQueryKey].
 */
@Suppress("unused")
open class InfiniteQueryId<T, S>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InfiniteQueryId<*, *>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "InfiniteQueryId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object
}

internal fun <T, S> InfiniteQueryKey<T, S>.hasMore(chunks: QueryChunks<T, S>): Boolean {
    return loadMoreParam(chunks) != null
}

/**
 * Function for building implementations of [InfiniteQueryKey] using [Kotlin Delegation](https://kotlinlang.org/docs/delegation.html).
 *
 * **Note:** By implementing through delegation, you can reduce the impact of future changes to [InfiniteQueryKey] interface extensions.
 *
 * Usage:
 *
 * ```kotlin
 * class GetPostsKey(userId: Int? = null) : InfiniteQueryKey<Posts, PageParam> by buildInfiniteQueryKey(
 *   id = Id(userId),
 *   fetch = { param -> ... }
 *   initialParam = { PageParam(limit = 20) },
 *   loadMoreParam = { chunks -> ... }
 * )
 * ```
 */
fun <T, S> buildInfiniteQueryKey(
    id: InfiniteQueryId<T, S>,
    fetch: suspend QueryReceiver.(param: S) -> T,
    initialParam: () -> S,
    loadMoreParam: (QueryChunks<T, S>) -> S?
): InfiniteQueryKey<T, S> {
    return object : InfiniteQueryKey<T, S> {
        override val id: InfiniteQueryId<T, S> = id
        override val fetch: suspend QueryReceiver.(param: S) -> T = fetch
        override val initialParam: () -> S = initialParam
        override val loadMoreParam: (QueryChunks<T, S>) -> S? = loadMoreParam
    }
}
