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
    val strategy: QueryCachingStrategy,
    val marker: Marker
) {

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder {
        var strategy: QueryCachingStrategy = Default.strategy
        var marker: Marker = Default.marker

        fun build() = QueryConfig(
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = QueryConfig(
            strategy = QueryCachingStrategy.Default,
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
