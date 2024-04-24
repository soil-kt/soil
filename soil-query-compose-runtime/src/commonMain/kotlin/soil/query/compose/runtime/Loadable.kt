// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

// For migration purposes only
@Stable
sealed interface Loadable<out T> {

    @Immutable
    data object Pending : Loadable<Nothing>

    @Immutable
    data class Fulfilled<T>(
        val data: T
    ) : Loadable<T>

    @Immutable
    data class Rejected(
        val error: Throwable
    ) : Loadable<Nothing>
}
