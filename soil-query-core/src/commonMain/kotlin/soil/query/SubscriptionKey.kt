// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.flow.Flow

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
     * Function to determine if the data should be cached.
     *
     * This function evaluates the provided data to decide whether it is suitable for caching.
     * If the function returns `true`, the data is considered cacheable; otherwise, it is not.
     * This can be useful in cases where caching is conditional based on specific data attributes
     * or properties that indicate the content should be stored temporarily.
     *
     * ```kotlin
     * override val contentCacheable: SubscriptionContentCacheable<SomeType> = { data -> data.isNotEmpty() }
     * ```
     */
    val contentCacheable: SubscriptionContentCacheable<T>? get() = null

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
     * Function to specify initial data.
     *
     * You can specify initial data instead of the initial loading state.
     *
     * ```kotlin
     * override fun onInitialData(): SubscriptionInitialData<User> = {
     *     getSubscriptionData(XxxSubscriptionKey.Id())
     * }
     * ```
     *
     * @see SubscriptionInitialData
     */
    fun onInitialData(): SubscriptionInitialData<T>? = null

    /**
     * Function to recover data from the error.
     *
     * You can recover data from the error instead of the error state.
     */
    fun onRecoverData(): SubscriptionRecoverData<T>? = null
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
