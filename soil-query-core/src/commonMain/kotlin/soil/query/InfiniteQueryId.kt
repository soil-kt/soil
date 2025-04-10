// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.TestTag
import soil.query.core.UniqueId

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

/**
 * TestTag implementation for InfiniteQuery operations that provides tag-based identification.
 *
 * This class is used to identify infinite queries for mocking in test and preview environments,
 * particularly when dealing with auto-generated query IDs that may change during
 * configuration changes or recompositions.
 *
 * Unlike [InfiniteQueryId] which might use auto-generated values that can change across
 * recompositions, InfiniteQueryTestTag provides a stable identifier that can be consistently
 * referenced in tests and previews.
 *
 * Usage example:
 * ```kotlin
 * // Define a test tag
 * class UserListQueryTestTag : InfiniteQueryTestTag<User, PageParams>("user-list")
 *
 * // Use with test client
 * testClient.on(UserListQueryTestTag()) { QueryState.success(mockUserChunks) }
 *
 * // Apply when getting an infinite query
 * val query = client.getInfiniteQuery(queryKey, Marker.testTag(UserListQueryTestTag()))
 * ```
 *
 * @param T The type of data that will be returned by the infinite query
 * @param S The type of pagination parameters for loading more data
 * @param tag A unique string identifier for this test tag
 */
@Suppress("unused")
open class InfiniteQueryTestTag<T, S>(override val tag: String) : TestTag {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InfiniteQueryTestTag<*, *>

        return tag == other.tag
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
