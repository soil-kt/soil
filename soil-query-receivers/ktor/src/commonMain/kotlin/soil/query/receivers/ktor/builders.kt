// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.receivers.ktor

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryChunks
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.buildInfiniteQueryKey
import soil.query.buildMutationKey
import soil.query.buildQueryKey
import soil.query.buildSubscriptionKey

/**
 * A delegation function to build a [MutationKey] for Ktor.
 *
 * ```kotlin
 * class CreatePostKey : MutationKey<Post, PostForm> by buildKtorMutationKey(
 *     mutate = { body ->
 *         post("https://jsonplaceholder.typicode.com/posts") {
 *             setBody(body)
 *         }.body()
 *     }
 * )
 * ```
 *
 * **Note:**
 * [httpClient] is required to use the builder functions designed for [soil.query.core.ContextReceiver].
 *
 * @param id The identifier of the mutation key.
 * @param mutate The mutation function that sends a request to the server.
 */
inline fun <T, S> buildKtorMutationKey(
    id: MutationId<T, S> = MutationId.auto(),
    crossinline mutate: suspend HttpClient.(variable: S) -> T
): MutationKey<T, S> = buildMutationKey(
    id = id,
    mutate = {
        val client = checkNotNull(httpClient) { "httpClient isn't available. Did you forget to set it up?" }
        with(client) { mutate(it) }
    }
)

/**
 * A delegation function to build a [QueryKey] for Ktor.
 *
 * ```kotlin
 * class GetPostKey(private val postId: Int) : QueryKey<Post> by buildKtorQueryKey(
 *     id = ..,
 *     fetch = {
 *         get("https://jsonplaceholder.typicode.com/posts/$postId").body()
 *     }
 * )
 * ```
 *
 * **Note:**
 * [httpClient] is required to use the builder functions designed for [soil.query.core.ContextReceiver].
 *
 * @param id The identifier of the query key.
 * @param fetch The query function that sends a request to the server.
 */
inline fun <T> buildKtorQueryKey(
    id: QueryId<T>,
    crossinline fetch: suspend HttpClient.() -> T
): QueryKey<T> = buildQueryKey(
    id = id,
    fetch = {
        val client = checkNotNull(httpClient) { "httpClient isn't available. Did you forget to set it up?" }
        with(client) { fetch() }
    }
)

/**
 * A delegation function to build an [InfiniteQueryKey] for Ktor.
 *
 * ```kotlin
 * class GetUserPostsKey(userId: Int) : InfiniteQueryKey<Posts, PageParam> by buildKtorInfiniteQueryKey(
 *     id = ..,
 *     fetch = { param ->
 *         get("https://jsonplaceholder.typicode.com/users/$userId/posts") {
 *             parameter("_start", param.offset)
 *             parameter("_limit", param.limit)
 *         }.body()
 *     },
 *     ...
 * )
 * ```
 *
 * **Note:**
 * [httpClient] is required to use the builder functions designed for [soil.query.core.ContextReceiver].
 *
 * @param id The identifier of the infinite query key.
 * @param fetch The query function that sends a request to the server.
 */
inline fun <T, S> buildKtorInfiniteQueryKey(
    id: InfiniteQueryId<T, S>,
    crossinline fetch: suspend HttpClient.(param: S) -> T,
    noinline initialParam: () -> S,
    noinline loadMoreParam: (QueryChunks<T, S>) -> S?
): InfiniteQueryKey<T, S> = buildInfiniteQueryKey(
    id = id,
    fetch = { param ->
        val client = checkNotNull(httpClient) { "httpClient isn't available. Did you forget to set it up?" }
        with(client) { fetch(param) }
    },
    initialParam = initialParam,
    loadMoreParam = loadMoreParam
)

/**
 * A delegation function to build a [SubscriptionKey] for Ktor.
 *
 * ```kotlin
 * class ExampleSubscriptionKey(auto: Namespace) : SubscriptionKey<String> by buildKtorSubscriptionKey(
 *     id = SubscriptionId(auto.value),
 *     subscribe = { // HttpClient.() -> Flow<String>
 *         doSomethingFlow()
 *     }
 * )
 * ```
 *
 * **Note:**
 * [httpClient] is required to use the builder functions designed for [soil.query.core.ContextReceiver].
 *
 * @param id The identifier of the subscription key.
 * @param subscribe The subscription function for receiving data, such as from a server.
 */
inline fun <T> buildKtorSubscriptionKey(
    id: SubscriptionId<T> = SubscriptionId.auto(),
    crossinline subscribe: HttpClient.() -> Flow<T>
): SubscriptionKey<T> = buildSubscriptionKey(
    id = id,
    subscribe = {
        val client = checkNotNull(httpClient) { "httpClient isn't available. Did you forget to set it up?" }
        with(client) { subscribe() }
    }
)
