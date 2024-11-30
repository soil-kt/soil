// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.EffectContext
import soil.query.core.EffectPropertyKey
import soil.query.core.UniqueId

typealias SubscriptionEffect = SubscriptionEffectClient.() -> Unit

/**
 * Interface for causing side effects on [Subscription] under the control of [SubscriptionClient].
 */
interface SubscriptionEffectClient : SubscriptionReadonlyClient {

    /**
     * Updates the data of the [SubscriptionKey] associated with the [id].
     */
    fun <T> updateSubscriptionData(
        id: SubscriptionId<T>,
        edit: T.() -> T
    )

    /**
     * Removes the subscriptions by the specified [RemoveSubscriptionsFilter].
     *
     * **Note:**
     * Subscriptions will be removed from [SubscriptionClient], but [SubscriptionRef] instances on the subscriber side will remain until they are dereferenced.
     * Also, the [kotlinx.coroutines.CoroutineScope] associated with the [kotlinx.coroutines.Job] will be canceled at the time of removal.
     */
    fun removeSubscriptions(filter: RemoveSubscriptionsFilter)

    /**
     * Removes the subscriptions by the specified [UniqueId].
     */
    fun <U : UniqueId> removeSubscriptionsBy(vararg ids: U)

    /**
     * Resumes the subscriptions by the specified [ResumeSubscriptionsFilter].
     */
    fun resumeSubscriptions(filter: ResumeSubscriptionsFilter)

    /**
     * Resumes the subscriptions by the specified [UniqueId].
     */
    fun <U : UniqueId> resumeSubscriptionsBy(vararg ids: U)
}

/**
 * Creates an [EffectContext] with the specified [SubscriptionEffectClient].
 */
fun EffectContext.withSubscription(block: SubscriptionEffect) = with(subscriptionClient, block)

/**
 * Gets the [SubscriptionEffectClient] from the [EffectContext].
 */
val EffectContext.subscriptionClient: SubscriptionEffectClient
    get() = this[subscriptionEffectClientPropertyKey]

internal val subscriptionEffectClientPropertyKey = EffectPropertyKey<SubscriptionEffectClient>(
    errorDescription = """
        SubscriptionEffectClient is not available.
        This might indicate an issue within the library, or it could be a setup issue.
        Ensure that you are using SwrClientPlus in your setup.
        If the issue persists, please report it to the library maintainers.
    """.trimIndent()
)
