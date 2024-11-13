// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBase
import soil.query.core.ContextReceiverBuilder
import soil.query.core.ContextReceiverBuilderBase

/**
 * Extension receiver for referencing external instances needed when executing query.
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
 * inline fun <T> buildCustomQueryKey(
 *     id: QueryId<T>,
 *     crossinline fetch: suspend CustomClient.() -> T
 * ): QueryKey<T> = buildQueryKey(
 *     id = id,
 *     fetch = {
 *         val client = checkNotNull(customClient) { "customClient isn't available. Did you forget to set it up?" }
 *         with(client) { fetch() }
 *     }
 * )
 * ```
 *
 * For receiver builder
 * ```kotlin
 * QueryReceiver {
 *     customClient = newCustomClient()
 * }
 * ```
 *
 * For receiver executor
 * ```kotlin
 * class GetPostKey(private val postId: Int) : QueryKey<Post> by buildCustomQueryKey(
 *     id = ..,
 *     fetch = { // CustomClient.() -> Post
 *         doSomething()
 *     }
 * )
 * ```
 */
interface QueryReceiver : ContextReceiver {

    /**
     * Default implementation for [QueryReceiver].
     */
    companion object : QueryReceiver {
        override fun <T : Any> get(key: ContextPropertyKey<T>): T? = null
    }
}

/**
 * Creates a [QueryReceiver] using the provided [builder].
 */
fun QueryReceiver(builder: QueryReceiverBuilder.() -> Unit): QueryReceiver {
    return QueryReceiverBuilderImpl().apply(builder).build()
}

/**
 * Builder for creating a [QueryReceiver].
 */
interface QueryReceiverBuilder : ContextReceiverBuilder

private class QueryReceiverImpl(
    context: Map<ContextPropertyKey<*>, Any>
) : ContextReceiverBase(context), QueryReceiver

private class QueryReceiverBuilderImpl : ContextReceiverBuilderBase(), QueryReceiverBuilder {
    override fun build(): QueryReceiver = QueryReceiverImpl(context)
}
