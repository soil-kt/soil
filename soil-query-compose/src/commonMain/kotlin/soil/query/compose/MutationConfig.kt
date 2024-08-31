// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import soil.query.core.Marker

/**
 * Configuration for the mutation.
 *
 * @property marker The marker with additional information based on the caller of a mutation.
 */
@Immutable
data class MutationConfig internal constructor(
    val strategy: MutationStrategy,
    val marker: Marker
) {

    class Builder {
        var strategy: MutationStrategy = Default.strategy
        var marker: Marker = Default.marker

        fun build() = MutationConfig(
            strategy = strategy,
            marker = marker
        )
    }

    companion object {
        val Default = MutationConfig(
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
