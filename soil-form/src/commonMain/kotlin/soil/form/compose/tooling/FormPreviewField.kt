package soil.form.compose.tooling

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import soil.form.FieldError
import soil.form.FieldName
import soil.form.FieldValidationMode
import soil.form.compose.FormField
import soil.form.noFieldError

class FormPreviewField<V>(
    value: V,
    error: FieldError = noFieldError,
    override val name: FieldName = "preview",
    isDirty: Boolean = false,
    isTouched: Boolean = false,
    isFocused: Boolean = false,
    isEnabled: Boolean = true
) : FormField<V> {
    override var value: V by mutableStateOf(value)
    override var error: FieldError by mutableStateOf(error)
    override var isDirty: Boolean by mutableStateOf(isDirty)
    override var isTouched: Boolean by mutableStateOf(isTouched)
    override var isFocused: Boolean by mutableStateOf(isFocused)
    override var isEnabled: Boolean by mutableStateOf(isEnabled)
    override fun onValueChange(value: V) {
        this.value = value
        isDirty = true
    }

    override fun onFocus() {
        isFocused = true
    }

    override fun onBlur() {
        isTouched = isTouched || isFocused
        isFocused = false
    }

    override fun handleFocus(hasFocus: Boolean) {
        when {
            hasFocus && !isFocused -> onFocus()
            !hasFocus && isFocused -> onBlur()
            else -> Unit
        }
    }

    override fun trigger(mode: FieldValidationMode) = Unit
}
