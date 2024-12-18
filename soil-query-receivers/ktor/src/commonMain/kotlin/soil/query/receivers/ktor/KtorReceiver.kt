// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.receivers.ktor

import io.ktor.client.HttpClient
import soil.query.MutationReceiver
import soil.query.QueryReceiver
import soil.query.SubscriptionReceiver
import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBuilder

/**
 * A receiver that uses Ktor to send queries and mutations.
 *
 * The `Receiver` interface can be specified as a parameter for [soil.query.SwrCachePolicy] when creating an instance of [soil.query.SwrCache].
 *
 * ```kotlin
 * val ktorClient = HttpClient {
 *     install(ContentNegotiation) {
 *         json()
 *     }
 * }
 * val receiver = KtorReceiver(client = ktorClient)
 * val swrCache = SwrCache(policy = SwrCachePolicy(
 *   ..,
 *   queryReceiver = receiver,
 *   mutationReceiver = receiver,
 *   ..
 * ))
 * ```
 *
 * If you use multiple receivers, create a single receiver that inherits from each of them.
 *
 * ```kotlin
 * class CustomReceiver(
 *     ktorClient: HttpClient,
 *     anotherClient: AnotherClient
 * ): KtorReceiver by KtorReceiver(ktorClient), AnotherReceiver by AnotherReceiver(anotherClient)
 * ```
 *
 * By setting the receiver, you can use the builder functions designed for [KtorReceiver] when defining query and mutation keys.
 * The `fetch/mutate` function block changes to the receiver type of the [HttpClient], allowing direct calls to [HttpClient]'s API.
 *
 * ```kotlin
 * class MyQueryKey: QueryKey<String> by buildKtorQueryKey(
 *     id = QueryId("myQuery"),
 *     fetch = { /* HttpClient.() -> String */
 *         get("https://example.com").body()
 *     }
 * )
 * ```
 *
 * @see buildKtorQueryKey
 * @see buildKtorInfiniteQueryKey
 * @see buildKtorMutationKey
 */
@Deprecated("Use standard receiver instead.", ReplaceWith(""))
interface KtorReceiver : QueryReceiver, MutationReceiver, SubscriptionReceiver {
    val ktorClient: HttpClient
}

/**
 * Creates a new receiver that uses the given [client] to send queries and mutations.
 *
 * @param client The Ktor client to use.
 */
@Deprecated("Use standard receiver instead.", ReplaceWith(""))
fun KtorReceiver(client: HttpClient): KtorReceiver {
    return KtorReceiverImpl(client)
}

internal class KtorReceiverImpl(
    ktorClient: HttpClient
) : KtorReceiver {

    private val context: Map<ContextPropertyKey<*>, Any> = mapOf(httpClientKey to ktorClient)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: ContextPropertyKey<T>): T? = context[key] as T?

    override val ktorClient: HttpClient
        get() = checkNotNull(httpClient)
}

/**
 * Extension receiver for referencing the [HttpClient] instance needed when executing query, mutation and subscription.
 *
 * @see ContextReceiver
 * @see buildKtorQueryKey
 * @see buildKtorInfiniteQueryKey
 * @see buildKtorMutationKey
 */
val ContextReceiver.httpClient: HttpClient?
    get() = get(httpClientKey)

/**
 * Extension receiver for setting the [HttpClient] instance needed when executing query, mutation and subscription.
 */
var ContextReceiverBuilder.httpClient: HttpClient
    get() = error("You cannot retrieve a builder property directly")
    set(value) = set(httpClientKey, value)

internal val httpClientKey = ContextPropertyKey<HttpClient>()
