// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

// TODO QueryModel implementation
/**
 * Promise-like data structure that represents the state of a value that is being loaded.
 *
 * Currently, this interface is intended for temporary use as a migration to queries.
 * Useful when combining the `query.compose.runtime` package with other asynchronous processing.
 *
 * @param T The type of the value that has been loaded.
 */
@Stable
sealed interface Loadable<out T> {

    /**
     * Represents the state of a value that is being loaded.
     */
    @Immutable
    data object Pending : Loadable<Nothing>

    /**
     * Represents the state of a value that has been loaded.
     */
    @Immutable
    data class Fulfilled<T>(
        val data: T
    ) : Loadable<T>

    /**
     * Represents the state of a value that has been rejected.
     */
    @Immutable
    data class Rejected(
        val error: Throwable
    ) : Loadable<Nothing>
}
