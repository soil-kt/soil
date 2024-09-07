// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the query.
 *
 * @property strategy The strategy for caching query data.
 * @property marker The marker with additional information based on the caller of a query.
 */
@Immutable
data class QueryConfig internal constructor(
    val strategy: QueryStrategy,
    val mapper: QueryObjectMapper,
    val marker: Marker
) {

    class Builder {
        var strategy: QueryStrategy = Default.strategy
        var mapper: QueryObjectMapper = Default.mapper
        var marker: Marker = Default.marker

        fun build() = QueryConfig(
            strategy = strategy,
            mapper = mapper,
            marker = marker
        )
    }

    companion object {
        val Default = QueryConfig(
            strategy = QueryStrategy.Default,
            mapper = QueryObjectMapper.Default,
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
