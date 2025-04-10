// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.core.uuid

/**
 * Unique identifier for [MutationKey].
 */
@Suppress("unused")
open class MutationId<T, S>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutationId<*, *>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "MutationId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object {

        /**
         * Automatically generates a [MutationId].
         *
         * Generates an ID for one-time use, so it cannot be shared among multiple places of use.
         *
         * FIXME: Since this function is for automatic ID assignment, it might be better not to have arguments.
         */
        @Deprecated(
            """
            This function is deprecated because it does not retain automatically generated values when used within Compose.
            As a result, values are regenerated after configuration changes, leading to different values.
            Consider using an alternative approach that preserves state across recompositions.
        """, ReplaceWith("MutationId(namespace, *tags)", "soil.query.MutationId")
        )
        fun <T, S> auto(
            namespace: String = "auto/${uuid()}",
            vararg tags: SurrogateKey
        ): MutationId<T, S> {
            return MutationId(namespace, *tags)
        }
    }
}

/**
 * TestTag implementation for Mutation operations that provides tag-based identification.
 *
 * This class is used to identify mutations for mocking in test and preview environments,
 * particularly when dealing with auto-generated mutation IDs that may change during
 * configuration changes or recompositions.
 *
 * Unlike [MutationId] which might use auto-generated values that can change across
 * recompositions, MutationTestTag provides a stable identifier that can be consistently
 * referenced in tests and previews.
 *
 * Usage example:
 * ```kotlin
 * // Define a test tag
 * class UpdateUserMutationTestTag : MutationTestTag<User, UserUpdateParams>("update-user")
 *
 * // Use with test client
 * testClient.on(UpdateUserMutationTestTag()) { updatedUser }
 *
 * // Apply when getting a mutation
 * val mutation = client.getMutation(mutationKey, Marker.testTag(UpdateUserMutationTestTag()))
 * ```
 *
 * @param T The type of data that will be returned by the mutation
 * @param S The type of input parameters for the mutation
 * @param tag A unique string identifier for this test tag
 */
@Suppress("unused")
open class MutationTestTag<T, S>(override val tag: String) : TestTag {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MutationTestTag<*, *>

        return tag == other.tag
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
