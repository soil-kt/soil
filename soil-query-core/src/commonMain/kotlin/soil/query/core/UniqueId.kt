// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Interface for unique identifiers.
 *
 * The unique identifier is a combination of a namespace and tags.
 * This [UniqueId] is used to cache the results of queries and mutations.
 */
interface UniqueId {

    /**
     * The namespace of this unique identifier.
     *
     * There are no specific formatting rules.
     * It's important for each resource to have a unique namespace, similar to URL paths.
     */
    val namespace: String

    /**
     * The tags of this unique identifier.
     *
     * If the [namespace] matches but the tags are different, they are treated as different cache entries.
     * Tags are useful for separating caches for variations based on query parameters or grouping for revalidation.
     *
     * `null` values are not allowed. Instead, represent them with compound types like [Pair].
     *
     * ```
     * val tags = arrayOf(
     *    "param1" to null,
     *    "param2" to 123
     * )
     * ```
     */
    val tags: Array<out SurrogateKey>
}


/**
 * A surrogate key for unique identifiers.
 */
typealias SurrogateKey = Any
