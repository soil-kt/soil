// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import soil.form.Field

/**
 * Adds a callback to be invoked when the focus state of the field changes.
 *
 * @param target The field to observe.
 * @return The applied modifier.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
fun Modifier.onFocusChanged(target: Field<*>): Modifier {
    return this then Modifier.onFocusChanged(target::onFocusChanged)
}

private fun Field<*>.onFocusChanged(state: FocusState) {
    val hasFocused = (state.isFocused || state.hasFocus) && !isFocused
    if (hasFocused) {
        onFocus()
        return
    }
    val hasBlurred = !(state.isFocused || state.hasFocus) && isFocused
    if (hasBlurred) {
        onBlur()
        return
    }
}
