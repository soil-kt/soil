// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBase
import soil.query.core.ContextReceiverBuilder
import soil.query.core.ContextReceiverBuilderBase

/**
 * Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
 *
 * Usage:
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
 * inline fun <T, S> buildCustomMutationKey(
 *     id: MutationId<T, S>,
 *     crossinline mutate: suspend CustomClient.(variable: S) -> T
 * ): MutationKey<T, S> = buildMutationKey(
 *     id = id,
 *     mutate = {
 *         val client = checkNotNull(customClient) { "customClient isn't available. Did you forget to set it up?" }
 *         with(client) { mutate(it) }
 *     }
 * )
 * ```
 *
 * For receiver builder
 * ```kotlin
 * MutationReceiver {
 *     customClient = newCustomClient()
 * }
 * ```
 *
 * For receiver executor
 * ```kotlin
 * class CreatePostKey : MutationKey<Post, PostForm> by buildCustomMutationKey(
 *     mutate = { body -> // CustomClient.(PostForm) -> Post
 *         doSomething(body)
 *     }
 * )
 * ```
 */
interface MutationReceiver : ContextReceiver {

    /**
     * Default implementation for [MutationReceiver].
     */
    companion object : MutationReceiver {
        override fun <T : Any> get(key: ContextPropertyKey<T>): T? = null
    }
}

/**
 * Creates a [MutationReceiver] using the provided [builder].
 */
fun MutationReceiver(builder: MutationReceiverBuilder.() -> Unit): MutationReceiver {
    return MutationReceiverBuilderImpl().apply(builder).build()
}

/**
 * Builder for creating a [MutationReceiver].
 */
interface MutationReceiverBuilder : ContextReceiverBuilder

private class MutationReceiverImpl(
    context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiverBase(context), MutationReceiver

private class MutationReceiverBuilderImpl : ContextReceiverBuilderBase(), MutationReceiverBuilder {
    override fun build(): MutationReceiver = MutationReceiverImpl(context)
}
