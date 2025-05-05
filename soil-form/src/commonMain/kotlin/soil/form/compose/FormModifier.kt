package soil.form.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

// NOTE: Context parameters will be introduced in Kotlin 2.2. The interface will be updated once it becomes stable.
fun Modifier.onFocusChanged(target: FormFieldControl<*>): Modifier {
    return this then Modifier.onFocusChanged { state ->
        with(target) {
            val hasFocused = (state.isFocused || state.hasFocus) && !isFocused
            if (hasFocused) {
                onFocus()
                return@with
            }
            val hasBlurred = !(state.isFocused || state.hasFocus) && isFocused
            if (hasBlurred) {
                onBlur()
                return@with
            }
        }
    }
}
