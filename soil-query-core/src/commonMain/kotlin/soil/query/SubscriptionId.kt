// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.SurrogateKey
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.core.uuid

/**
 * Unique identifier for [SubscriptionKey].
 */
@Suppress("unused")
open class SubscriptionId<T>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SubscriptionId<*>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "SubscriptionId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object {
        @Deprecated(
            """
            This function is deprecated because it does not retain automatically generated values when used within Compose.
            As a result, values are regenerated after configuration changes, leading to different values.
            Consider using an alternative approach that preserves state across recompositions.
        """, ReplaceWith("SubscriptionId(namespace, *tags)", "soil.query.SubscriptionId")
        )
        fun <T> auto(
            namespace: String = "auto/${uuid()}",
            vararg tags: SurrogateKey
        ): SubscriptionId<T> {
            return SubscriptionId(namespace, *tags)
        }
    }
}

/**
 * TestTag implementation for Subscription operations that provides tag-based identification.
 *
 * This class is used to identify subscriptions for mocking in test and preview environments,
 * particularly when dealing with auto-generated subscription IDs that may change during
 * configuration changes or recompositions.
 *
 * Unlike [SubscriptionId] which might use auto-generated values that can change across
 * recompositions, SubscriptionTestTag provides a stable identifier that can be consistently
 * referenced in tests and previews.
 *
 * Usage example:
 * ```kotlin
 * // Define a test tag
 * class UserUpdatesTestTag : SubscriptionTestTag<User>("user-updates")
 *
 * // Use with test client
 * testClient.on(UserUpdatesTestTag()) { MutableStateFlow(mockUser) }
 *
 * // Apply when getting a subscription
 * val subscription = client.getSubscription(subscriptionKey, Marker.testTag(UserUpdatesTestTag()))
 * ```
 *
 * @param T The type of data that will be received by the subscription
 * @param tag A unique string identifier for this test tag
 */
@Suppress("unused")
open class SubscriptionTestTag<T>(override val tag: String) : TestTag {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SubscriptionTestTag<*>

        return tag == other.tag
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
