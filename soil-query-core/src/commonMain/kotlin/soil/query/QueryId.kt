// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.TestTag
import soil.query.core.UniqueId

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
 * TestTag implementation for Query operations that provides tag-based identification.
 * 
 * This class is used to identify queries for mocking in test and preview environments,
 * particularly when dealing with auto-generated query IDs that may change during
 * configuration changes or recompositions.
 * 
 * Unlike [QueryId] which might use auto-generated values that can change across
 * recompositions, QueryTestTag provides a stable identifier that can be consistently
 * referenced in tests and previews.
 * 
 * Usage example:
 * ```kotlin
 * // Define a test tag
 * class UserQueryTestTag : QueryTestTag<User>("user-query")
 * 
 * // Use with test client
 * testClient.on(UserQueryTestTag()) { QueryState.success(mockUser) }
 * 
 * // Apply when getting a query
 * val query = client.getQuery(queryKey, Marker.testTag(UserQueryTestTag()))
 * ```
 * 
 * @param T The type of data that will be returned by the query
 * @param tag A unique string identifier for this test tag
 */
@Suppress("unused")
open class QueryTestTag<T>(override val tag: String) : TestTag {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QueryTestTag<*>

        return tag == other.tag
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
