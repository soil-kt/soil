// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.SubscriptionClient
import soil.query.SubscriptionKey
import soil.query.SubscriptionRef
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.internal.newSubscription

/**
 * Remember a [SubscriptionObject] and subscribes to the subscription state of [key].
 *
 * @param T Type of data to receive.
 * @param key The [SubscriptionKey] for managing [subscription][soil.query.Subscription].
 * @param config The configuration for the subscription. By default, it uses the [SubscriptionConfig.Default].
 * @param client The [SubscriptionClient] to resolve [key]. By default, it uses the [LocalSubscriptionClient].
 * @return A [SubscriptionObject] each the subscription state changed.
 */
@ExperimentalSoilQueryApi
@Composable
fun <T> rememberSubscription(
    key: SubscriptionKey<T>,
    config: SubscriptionConfig = SubscriptionConfig.Default,
    client: SubscriptionClient = LocalSubscriptionClient.current
): SubscriptionObject<T> {
    val scope = rememberCoroutineScope()
    val subscription = remember(key.id) { newSubscription(key, config, client, scope) }
    subscription.Effect()
    return with(config.mapper) {
        config.strategy.collectAsState(subscription).toObject(subscription = subscription, select = { it })
    }
}

/**
 * Remember a [SubscriptionObject] and subscribes to the subscription state of [key].
 *
 * @param T Type of data to receive.
 * @param U Type of selected data.
 * @param key The [SubscriptionKey] for managing [subscription][soil.query.Subscription].
 * @param select A function to select data from [T].
 * @param config The configuration for the subscription. By default, it uses the [SubscriptionConfig.Default].
 * @param client The [SubscriptionClient] to resolve [key]. By default, it uses the [LocalSubscriptionClient].
 * @return A [SubscriptionObject] with selected data each the subscription state changed.
 */
@ExperimentalSoilQueryApi
@Composable
fun <T, U> rememberSubscription(
    key: SubscriptionKey<T>,
    select: (T) -> U,
    config: SubscriptionConfig = SubscriptionConfig.Default,
    client: SubscriptionClient = LocalSubscriptionClient.current
): SubscriptionObject<U> {
    val scope = rememberCoroutineScope()
    val subscription = remember(key.id) { newSubscription(key, config, client, scope) }
    subscription.Effect()
    return with(config.mapper) {
        config.strategy.collectAsState(subscription).toObject(subscription = subscription, select = select)
    }
}

@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
@Composable
private inline fun SubscriptionRef<*>.Effect() {
    // TODO: Switch to LifecycleResumeEffect
    //  Android, it works only with Compose UI 1.7.0-alpha05 or above.
    //  Therefore, we will postpone adding this code until a future release.
    LaunchedEffect(id) {
        join()
    }
}
