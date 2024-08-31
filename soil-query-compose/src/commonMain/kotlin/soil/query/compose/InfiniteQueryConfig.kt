// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the infinite query.
 *
 * @property strategy The strategy for caching query data.
 * @property marker The marker with additional information based on the caller of a query.
 */
@Immutable
data class InfiniteQueryConfig internal constructor(
    val strategy: InfiniteQueryStrategy,
    val marker: Marker
) {

    class Builder {
        var strategy: InfiniteQueryStrategy = Default.strategy
        var marker: Marker = Default.marker

        fun build() = InfiniteQueryConfig(
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = InfiniteQueryConfig(
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
