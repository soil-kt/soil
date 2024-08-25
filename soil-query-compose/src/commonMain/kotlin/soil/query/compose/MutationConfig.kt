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
    val marker: Marker
) {

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder {
        val marker: Marker = Default.marker

        fun build() = MutationConfig(
            marker = marker
        )
    }

    companion object {
        val Default = MutationConfig(
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
