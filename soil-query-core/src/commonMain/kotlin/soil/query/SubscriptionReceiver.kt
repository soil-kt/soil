// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBase
import soil.query.core.ContextReceiverBuilder
import soil.query.core.ContextReceiverBuilderBase

/**
 * Extension receiver for referencing external instances needed when receiving subscription.
 *
 * For receiver provider
 * ```kotlin
 * internal val customClientKey = ContextPropertyKey<CustomClient>()
 *
 * val ContextReceiver.customClient: CustomClient?
 *     get() = get(customClientKey)
 *
 * var ContextReceiverBuilder.customClient: CustomClient
 *     get() = error("You cannot retrieve a builder property directly.")
 *     set(value) = set(customClientKey, value)
 *
 * inline fun <T> buildCustomSubscriptionKey(
 *     id: SubscriptionId<T>,
 *     crossinline subscribe: CustomClient.() -> Flow<T>
 * ): SubscriptionKey<T> = buildSubscriptionKey(
 *     id = id,
 *     subscribe = {
 *         val client = checkNotNull(customClient) { "customClient isn't available. Did you forget to set it up?" }
 *         with(client) { subscribe() }
 *     }
 * )
 * ```
 *
 * For receiver builder
 * ```kotlin
 * SubscriptionReceiver {
 *     customClient = newCustomClient()
 * }
 * ```
 *
 * For receiver executor
 * ```kotlin
 * class ExampleSubscriptionKey(auto: Namespace) : SubscriptionKey<String> by buildCustomSubscriptionKey(
 *     id = SubscriptionId(auto.value),
 *     subscribe = { // CustomClient.() -> Flow<String>
 *         doSomethingFlow()
 *     }
 * )
 * ```
 */
interface SubscriptionReceiver : ContextReceiver {

    /**
     * Default implementation for [SubscriptionReceiver].
     */
    companion object : SubscriptionReceiver {
        override fun <T : Any> get(key: ContextPropertyKey<T>): T? = null
    }
}

fun SubscriptionReceiver(builder: SubscriptionReceiverBuilder.() -> Unit): SubscriptionReceiver {
    return SubscriptionReceiverBuilderImpl().apply(builder).build()
}

interface SubscriptionReceiverBuilder : ContextReceiverBuilder

private class SubscriptionReceiverImpl(
    context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiverBase(context), SubscriptionReceiver

private class SubscriptionReceiverBuilderImpl : ContextReceiverBuilderBase(), SubscriptionReceiverBuilder {
    override fun build(): SubscriptionReceiver = SubscriptionReceiverImpl(context)
}
