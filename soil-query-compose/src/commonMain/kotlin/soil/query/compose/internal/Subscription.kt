// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.SubscriptionClient
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionRef
import soil.query.SubscriptionState
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SubscriptionConfig

@ExperimentalSoilQueryApi
internal fun <T> newSubscription(
    key: SubscriptionKey<T>,
    config: SubscriptionConfig,
    client: SubscriptionClient,
    scope: CoroutineScope
): SubscriptionRef<T> = Subscription(key, config, client, scope)

@ExperimentalSoilQueryApi
private class Subscription<T>(
    key: SubscriptionKey<T>,
    config: SubscriptionConfig,
    client: SubscriptionClient,
    private val scope: CoroutineScope
) : SubscriptionRef<T>, RememberObserver {

    private val subscription: SubscriptionRef<T> = client.getSubscription(key, config.marker)
    private val optimize: (SubscriptionState<T>) -> SubscriptionState<T> = config.optimizer::omit

    override val id: SubscriptionId<T> = subscription.id

    private val _state: MutableStateFlow<SubscriptionState<T>> = MutableStateFlow(
        value = optimize(subscription.state.value)
    )
    override val state: StateFlow<SubscriptionState<T>> = _state

    override fun close() = subscription.close()

    override suspend fun reset() = subscription.reset()

    override suspend fun resume() = subscription.resume()

    // ----- RememberObserver -----//
    private var job: Job? = null

    override fun onAbandoned() = stop()

    override fun onForgotten() = stop()

    override fun onRemembered() = start()

    private fun start() {
        job = scope.launch {
            subscription.state.collect { _state.value = optimize(it) }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        close()
    }
}
