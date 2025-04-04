// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable

/**
 * A scope to manage the state and actions of input fields in a form.
 *
 * @param T The type of the form value.
 */
@Stable
class FormScope<T : Any>(
    val state: FormState<T>,
    val handleSubmit: () -> Unit
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FormScope<*>

        if (state != other.state) return false
        if (handleSubmit != other.handleSubmit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + handleSubmit.hashCode()
        return result
    }
}
