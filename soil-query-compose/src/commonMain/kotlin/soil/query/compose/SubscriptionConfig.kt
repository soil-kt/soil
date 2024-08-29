// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the subscription.
 *
 * @property strategy The strategy for caching subscription data.
 * @property marker The marker with additional information based on the caller of a subscription.
 */
@Immutable
data class SubscriptionConfig internal constructor(
    val strategy: SubscriptionStrategy,
    val marker: Marker
) {

    class Builder {
        var strategy: SubscriptionStrategy = SubscriptionStrategy.Default
        var marker: Marker = Default.marker

        fun build() = SubscriptionConfig(
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = SubscriptionConfig(
            strategy = SubscriptionStrategy.Default,
            marker = Marker.None
        )
    }
}

/**
 * Creates a [SubscriptionConfig] with the provided [initializer].
 */
fun SubscriptionConfig(initializer: SubscriptionConfig.Builder.() -> Unit): SubscriptionConfig {
    return SubscriptionConfig.Builder().apply(initializer).build()
}
