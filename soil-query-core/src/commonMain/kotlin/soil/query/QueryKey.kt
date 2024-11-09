// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.UniqueId

/**
 * [QueryKey] for managing [Query] associated with [id].
 *
 * @param T Type of data to retrieve.
 */
interface QueryKey<T> {

    /**
     * A unique identifier used for managing [QueryKey].
     */
    val id: QueryId<T>

    /**
     * Suspending function to retrieve data.
     *
     * @receiver QueryReceiver You can use a custom QueryReceiver within the fetch function.
     */
    val fetch: suspend QueryReceiver.() -> T

    /**
     * Function to compare the content of the data.
     *
     * This function is used to determine whether the data is identical to the previous data via [QueryCommand].
     * If the data is considered the same, only [QueryState.staleAt] is updated.
     * This can be useful when strict update management is needed, such as when special comparison is necessary,
     * although it is generally not that important.
     *
     * ```kotlin
     * override val contentEquals: QueryContentEquals<SomeType> = { a, b -> a.xx == b.xx }
     * ```
     */
    val contentEquals: QueryContentEquals<T>? get() = null

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
    val contentCacheable: QueryContentCacheable<T>? get() = null

    /**
     * Function to configure the [QueryOptions].
     *
     * If unspecified, the default value of [SwrCachePolicy] is used.
     *
     * ```kotlin
     * override fun onConfigureOptions(): QueryOptionsOverride = { options ->
     *     options.copy(gcTime = Duration.ZERO)
     * }
     * ```
     */
    fun onConfigureOptions(): QueryOptionsOverride? = null

    /**
     * Function to specify initial data.
     *
     * You can specify initial data instead of the initial loading state.
     *
     * ```kotlin
     * override fun onInitialData(): QueryInitialData<User> = {
     *     getInfiniteQueryData(GetUsersKey.Id())?.let {
     *         it.chunkedData.firstOrNull { user -> user.id == userId }
     *     }
     * }
     * ```
     *
     * @see QueryInitialData
     */
    fun onInitialData(): QueryInitialData<T>? = null

    /**
     * Function to convert specific exceptions as data.
     *
     * Depending on the type of exception that occurred during data retrieval, it is possible to recover it as normal data.
     *
     * ```kotlin
     * override fun onRecoverData(): QueryRecoverData<QueryChunks<Albums, PageParam>> = { err ->
     *     if (err is ClientRequestException && err.response.status.value == 404) {
     *         emptyList()
     *     } else {
     *         throw err
     *     }
     * }
     * ```
     *
     * @see QueryRecoverData
     */
    fun onRecoverData(): QueryRecoverData<T>? = null
}

/**
 * Unique identifier for [QueryKey].
 */
@Suppress("unused")
open class QueryId<T>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryId<*>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "QueryId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object
}

/**
 * Function for building implementations of [QueryKey] using [Kotlin Delegation](https://kotlinlang.org/docs/delegation.html).
 *
 * **Note:** By implementing through delegation, you can reduce the impact of future changes to [QueryKey] interface extensions.
 *
 * Usage:
 *
 * ```kotlin
 * class GetPostKey(private val postId: Int) : QueryKey<Post> by buildQueryKey(
 *   id = Id(postId),
 *   fetch = { ... }
 * )
 * ```
 */
fun <T> buildQueryKey(
    id: QueryId<T>,
    fetch: suspend QueryReceiver.() -> T
): QueryKey<T> {
    return object : QueryKey<T> {
        override val id: QueryId<T> = id
        override val fetch: suspend QueryReceiver.() -> T = fetch
    }
}
