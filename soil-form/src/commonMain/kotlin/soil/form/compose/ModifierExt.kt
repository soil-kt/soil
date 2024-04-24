// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import soil.form.Field

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
