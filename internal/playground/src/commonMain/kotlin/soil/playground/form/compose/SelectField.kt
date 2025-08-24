package soil.playground.form.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import org.jetbrains.compose.ui.tooling.preview.Preview
import soil.form.FieldValidator
import soil.form.compose.FormField
import soil.form.compose.hasError
import soil.form.compose.tooling.PreviewField
import soil.form.rule.notNull
import soil.playground.form.Title
import soil.playground.style.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormField<T?>.SelectField(
    transform: (T?) -> String,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    content: @Composable SelectFieldScope<T>.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier.onFocusChanged { handleFocus(it.isFocused || it.hasFocus) }
    ) {
        OutlinedTextField(
            value = transform(value),
            onValueChange = { /* no-op: read-only */ },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, isEnabled)
                .onFocusEvent { state ->
                    if (state.isFocused && value == null) {
                        isExpanded = true
                    }
                },
            isError = hasError,
            enabled = isEnabled,
            readOnly = true,
            supportingText = supportingText,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon ?: {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            val scope = remember { SelectFieldScope(this@SelectField) { isExpanded = it } }
            scope.content()
        }
    }
}

@Stable
class SelectFieldScope<T : Any>(
    @PublishedApi internal val field: FormField<T?>,
    @PublishedApi internal val onExpandedChange: (Boolean) -> Unit
) {

    @Suppress("NOTHING_TO_INLINE")
    @Composable
    inline fun Option(
        value: T,
        modifier: Modifier = Modifier,
        noinline leadingIcon: @Composable (() -> Unit)? = null,
        noinline trailingIcon: @Composable (() -> Unit)? = null,
        enabled: Boolean = true,
        noinline content: @Composable () -> Unit
    ) {
        key(value) {
            DropdownMenuItem(
                text = content,
                onClick = {
                    field.onValueChange(value)
                    onExpandedChange(false)
                },
                modifier = modifier,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                enabled = enabled
            )
        }
    }
}

@Preview
@Composable
private fun SelectFieldPreview() {
    AppTheme {
        PreviewField<Title?>(
            initialValue = null,
            validator = FieldValidator {
                notNull { "This field is required" }
            },
            render = { field ->
                field.WithLayout {
                    SelectField(transform = { it?.name ?: "-" }) {
                        Title.entries.forEach { value ->
                            Option(value) {
                                Text(text = value.name)
                            }
                        }
                    }
                }
            }
        )
    }
}
