package soil.playground.form.compose

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.VisualTransformation
import soil.form.compose.FormField
import soil.form.compose.hasError
import soil.playground.style.AppTheme

@Composable
fun InputField(
    ref: FormField<String>,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = ref.value,
        onValueChange = ref::onValueChange,
        modifier = modifier.onFocusChanged { ref.handleFocus(it.isFocused || it.hasFocus) },
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = ref.isEnabled,
        isError = ref.hasError,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        supportingText = supportingText ?: {
            if (ref.hasError) {
                Text(text = ref.error.messages.first(), color = AppTheme.colorScheme.error)
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}
