// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the infinite query.
 *
 * @property mapper The mapper for converting query data.
 * @property optimizer The optimizer for recomposing the query data.
 * @property strategy The strategy for caching query data.
 * @property marker The marker with additional information based on the caller of a query.
 */
@Immutable
data class InfiniteQueryConfig internal constructor(
    val mapper: InfiniteQueryObjectMapper,
    val optimizer: InfiniteQueryRecompositionOptimizer,
    val strategy: InfiniteQueryStrategy,
    val marker: Marker
) {

    /**
     * Creates a new [InfiniteQueryConfig] with the provided [block].
     */
    fun builder(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(config: InfiniteQueryConfig = Default) {
        var mapper: InfiniteQueryObjectMapper = config.mapper
        var optimizer: InfiniteQueryRecompositionOptimizer = config.optimizer
        var strategy: InfiniteQueryStrategy = config.strategy
        var marker: Marker = config.marker

        fun build() = InfiniteQueryConfig(
            strategy = strategy,
            optimizer = optimizer,
            mapper = mapper,
            marker = marker
        )
    }

    companion object {
        val Default = InfiniteQueryConfig(
            mapper = InfiniteQueryObjectMapper.Default,
            optimizer = InfiniteQueryRecompositionOptimizer.Enabled,
            strategy = InfiniteQueryStrategy.Default,
            marker = Marker.None
        )
    }
}

/**
 * Creates a [InfiniteQueryConfig] with the provided [initializer].
 */
fun InfiniteQueryConfig(initializer: InfiniteQueryConfig.Builder.() -> Unit): InfiniteQueryConfig {
    return InfiniteQueryConfig.Builder().apply(initializer).build()
}
