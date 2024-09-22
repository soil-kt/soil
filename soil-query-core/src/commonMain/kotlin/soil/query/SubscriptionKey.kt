// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.flow.Flow
import soil.query.core.SurrogateKey
import soil.query.core.UniqueId
import soil.query.core.uuid

/**
 * [SubscriptionKey] for managing [Subscription] associated with [id].
 *
 * @param T Type of data to receive.
 */
interface SubscriptionKey<T> {

    /**
     * A unique identifier used for managing [SubscriptionKey].
     */
    val id: SubscriptionId<T>

    /**
     * Function to subscribe to the data as [Flow].
     *
     * @receiver SubscriptionReceiver You can use a custom SubscriptionReceiver within the subscribe function.
     */
    val subscribe: SubscriptionReceiver.() -> Flow<T>

    /**
     * Function to compare the content of the data.
     *
     * This function is used to determine whether the data is identical to the previous data via [SubscriptionCommand].
     * If the data is considered the same, [SubscriptionState.replyUpdatedAt] is not updated, and the existing reply state is maintained.
     * This can be useful when strict update management is needed, such as when special comparison is necessary,
     * although it is generally not that important.
     *
     * ```kotlin
     * override val contentEquals: SubscriptionContentEquals<SomeType> = { a, b -> a.xx == b.xx }
     * ```
     */
    val contentEquals: SubscriptionContentEquals<T>? get() = null

    /**
     * Function to configure the [SubscriptionOptions].
     *
     * If unspecified, the default value of [SubscriptionOptions] is used.
     *
     * ```kotlin
     * override fun onConfigureOptions(): SubscriptionOptionsOverride = { options ->
     *     options.copy(gcTime = Duration.ZERO)
     * }
     * ```
     */
    fun onConfigureOptions(): SubscriptionOptionsOverride? = null

    /**
     * Function to recover data from the error.
     *
     * You can recover data from the error instead of the error state.
     */
    fun onRecoverData(): SubscriptionRecoverData<T>? = null
}

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
        fun <T> auto(
            namespace: String = "auto/${uuid()}",
            vararg tags: SurrogateKey
        ): SubscriptionId<T> {
            return SubscriptionId(namespace, *tags)
        }
    }
}

/**
 * Function for building implementations of [SubscriptionKey] using [Kotlin Delegation](https://kotlinlang.org/docs/delegation.html).
 *
 * **Note:** By implementing through delegation, you can reduce the impact of future changes to [SubscriptionKey] interface extensions.
 *
 * Usage:
 *
 * ```kotlin
 * class UserSubscriptionKey(private val userId: String) : SubscriptionKey<User> by buildSubscriptionKey(
 *   id = Id(userId),
 *   subscribe = { ... }
 * )
 * ```
 */
fun <T> buildSubscriptionKey(
    id: SubscriptionId<T> = SubscriptionId.auto(),
    subscribe: SubscriptionReceiver.() -> Flow<T>
): SubscriptionKey<T> {
    return object : SubscriptionKey<T> {
        override val id: SubscriptionId<T> = id
        override val subscribe: SubscriptionReceiver.() -> Flow<T> = subscribe
    }
}
