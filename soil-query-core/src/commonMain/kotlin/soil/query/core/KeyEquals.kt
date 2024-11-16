// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * An optional base class for determining key equivalence.
 *
 * Usage:
 * ```
 * private class TestQueryKey : KeyEquals(), QueryKey<String> by buildQueryKey(
 *   id = ..
 * )
 * ```
 *
 * **Note:**
 * When using this on Compose with StrongSkippingMode enabled, you can generate the key without wrapping it in the `remember` function if the key can be marked as stable.
 * For details on marking objects as stable, refer to the official documentation:
 * https://developer.android.com/develop/ui/compose/performance/stability
 */
abstract class KeyEquals {
    abstract val id: UniqueId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KeyEquals

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
