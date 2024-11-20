// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the query.
 *
 * @property mapper The mapper for converting query data.
 * @property optimizer The optimizer for recomposing the query data.
 * @property strategy The strategy for caching query data.
 * @property marker The marker with additional information based on the caller of a query.
 */
@Immutable
data class QueryConfig internal constructor(
    val mapper: QueryObjectMapper,
    val optimizer: QueryRecompositionOptimizer,
    val strategy: QueryStrategy,
    val marker: Marker
) {

    /**
     * Creates a new [QueryConfig] with the provided [block].
     */
    fun builder(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    class Builder(config: QueryConfig = Default) {
        var mapper: QueryObjectMapper = config.mapper
        var optimizer: QueryRecompositionOptimizer = config.optimizer
        var strategy: QueryStrategy = config.strategy
        var marker: Marker = config.marker

        fun build() = QueryConfig(
            mapper = mapper,
            optimizer = optimizer,
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = QueryConfig(
            mapper = QueryObjectMapper.Default,
            optimizer = QueryRecompositionOptimizer.Enabled,
            strategy = QueryStrategy.Default,
            marker = Marker.None
        )
    }
}

/**
 * Creates a [QueryConfig] with the provided [initializer].
 */
fun QueryConfig(initializer: QueryConfig.Builder.() -> Unit): QueryConfig {
    return QueryConfig.Builder().apply(initializer).build()
}
