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
     * Function to configure the [QueryOptions].
     *
     * If unspecified, the default value of [SwrCachePolicy] is used.
     */
    fun onConfigureOptions(): QueryOptionsOverride? = null

    /**
     * Function to convert specific exceptions as data.
     *
     * Depending on the type of exception that occurred during data retrieval, it is possible to recover it as normal data.
     *
     * @see QueryRecoverData
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
