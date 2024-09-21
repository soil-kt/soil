// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the subscription.
 *
 * @property mapper The mapper for converting subscription data.
 * @property optimizer The optimizer for recomposing the subscription data.
 * @property strategy The strategy for caching subscription data.
 * @property marker The marker with additional information based on the caller of a subscription.
 */
@Immutable
data class SubscriptionConfig internal constructor(
    val mapper: SubscriptionObjectMapper,
    val optimizer: SubscriptionRecompositionOptimizer,
    val strategy: SubscriptionStrategy,
    val marker: Marker
) {

    /**
     * Creates a new [SubscriptionConfig] with the provided [block].
     */
    fun builder(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(config: SubscriptionConfig = Default) {
        var mapper: SubscriptionObjectMapper = config.mapper
        var optimizer: SubscriptionRecompositionOptimizer = config.optimizer
        var strategy: SubscriptionStrategy = config.strategy
        var marker: Marker = config.marker

        fun build() = SubscriptionConfig(
            mapper = mapper,
            optimizer = optimizer,
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = SubscriptionConfig(
            mapper = SubscriptionObjectMapper.Default,
            optimizer = SubscriptionRecompositionOptimizer.Default,
            strategy = SubscriptionStrategy.Default,
            marker = Marker.None
        )

        val Lazy = SubscriptionConfig(
            mapper = SubscriptionObjectMapper.Default,
            optimizer = SubscriptionRecompositionOptimizer.Lazy,
            strategy = SubscriptionStrategy.Lazy,
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
