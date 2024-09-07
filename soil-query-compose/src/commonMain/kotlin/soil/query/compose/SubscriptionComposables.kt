// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.query.SubscriptionClient
import soil.query.SubscriptionKey
import soil.query.annotation.ExperimentalSoilQueryApi

/**
 * Provides a conditional [rememberSubscription].
 *
 * Calls [rememberSubscription] only if [keyFactory] returns a [SubscriptionKey] from [value].
 *
 * @see rememberSubscription
 */
@ExperimentalSoilQueryApi
@Composable
fun <T, V> rememberSubscriptionIf(
    value: V,
    keyFactory: (value: V) -> SubscriptionKey<T>?,
    config: SubscriptionConfig = SubscriptionConfig.Default,
    client: SubscriptionClient = LocalSubscriptionClient.current
): SubscriptionObject<T>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberSubscription(key, config, client)
}

/**
 * Provides a conditional [rememberSubscription].
 *
 * Calls [rememberSubscription] only if [keyFactory] returns a [SubscriptionKey] from [value].
 *
 * @see rememberSubscription
 */
@ExperimentalSoilQueryApi
@Composable
fun <T, U, V> rememberSubscriptionIf(
    value: V,
    keyFactory: (value: V) -> SubscriptionKey<T>?,
    select: (T) -> U,
    config: SubscriptionConfig = SubscriptionConfig.Default,
    client: SubscriptionClient = LocalSubscriptionClient.current
): SubscriptionObject<U>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberSubscription(key, select, config, client)
}
