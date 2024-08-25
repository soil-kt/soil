// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * An interface for providing additional information based on the caller of a query or mutation.
 *
 * **Note:**
 * You can include different information in the [ErrorRecord] depending on where the key is used.
 * This is useful when you want to differentiate error messages based on the specific use case,
 * even if the same query or mutation is used in multiple places.
 */
interface Marker {
    companion object None : Marker
}
