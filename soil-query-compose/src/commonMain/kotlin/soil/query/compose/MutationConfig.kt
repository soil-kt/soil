// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the mutation.
 *
 * @property mapper The mapper for converting mutation data.
 * @property optimizer The optimizer for recomposing the mutation data.
 * @property strategy The strategy for caching mutation data.
 * @property marker The marker with additional information based on the caller of a mutation.
 */
@Immutable
data class MutationConfig internal constructor(
    val mapper: MutationObjectMapper,
    val optimizer: MutationRecompositionOptimizer,
    val strategy: MutationStrategy,
    val marker: Marker
) {

    /**
     * Creates a new [MutationConfig] with the provided [block].
     */
    fun builder(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(config: MutationConfig = Default) {
        var mapper: MutationObjectMapper = config.mapper
        var optimizer: MutationRecompositionOptimizer = config.optimizer
        var strategy: MutationStrategy = config.strategy
        var marker: Marker = config.marker

        fun build() = MutationConfig(
            mapper = mapper,
            optimizer = optimizer,
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = MutationConfig(
            mapper = MutationObjectMapper.Default,
            optimizer = MutationRecompositionOptimizer.Enabled,
            strategy = MutationStrategy.Default,
            marker = Marker.None
        )
    }
}

/**
 * Creates a [MutationConfig] with the provided [initializer].
 */
fun MutationConfig(initializer: MutationConfig.Builder.() -> Unit): MutationConfig {
    return MutationConfig.Builder().apply(initializer).build()
}
