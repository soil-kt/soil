package soil.playground.form.compose

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.ui.tooling.preview.Preview
import soil.form.FieldValidator
import soil.form.compose.FormField
import soil.form.compose.hasError
import soil.form.compose.tooling.PreviewField
import soil.form.rule.notBlank
import soil.playground.style.AppTheme

@Composable
fun FormField<String>.InputField(
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
        value = value,
        onValueChange = ::onValueChange,
        modifier = modifier.onFocusChanged { handleFocus(it.isFocused || it.hasFocus) },
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = isEnabled,
        isError = hasError,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

@Preview
@Composable
private fun InputFieldPreview() {
    AppTheme {
        PreviewField(
            initialValue = "",
            validator = FieldValidator {
                notBlank { "This field cannot be blank" }
            },
            render = { field ->
                field.WithLayout {
                    InputField(label = { Text("Name") })
                }
            }
        )
    }
}
