// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.receivers.ktor

import io.ktor.client.HttpClient
import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBuilder


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
