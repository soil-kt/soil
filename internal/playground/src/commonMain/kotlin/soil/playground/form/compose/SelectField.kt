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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import soil.form.compose.FormField
import soil.form.compose.hasError
import soil.playground.style.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SelectField(
    ref: FormField<T?>,
    value: (T?) -> String,
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
    ) {
        OutlinedTextField(
            value = value(ref.value),
            onValueChange = { /* no-op: read-only */ },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, ref.isEnabled)
                .onFocusChanged { ref.handleFocus(it.isFocused || it.hasFocus) }
                .onFocusEvent { state ->
                    if (state.isFocused && ref.value == null) {
                        isExpanded = true
                    }
                },
            isError = ref.hasError,
            enabled = ref.isEnabled,
            readOnly = true,
            supportingText = supportingText ?: {
                if (ref.hasError) {
                    Text(text = ref.error.messages.first(), color = AppTheme.colorScheme.error)
                }
            },
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
            val scope = remember(ref) { SelectFieldScope(ref) { isExpanded = it } }
            scope.content()
        }
    }
}

@Stable
class SelectFieldScope<T : Any>(
    private val ref: FormField<T?>,
    private val onExpandedChange: (Boolean) -> Unit
) {

    @Composable
    fun Option(
        value: T,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        DropdownMenuItem(
            text = content,
            onClick = {
                ref.onValueChange(value)
                onExpandedChange(false)
            },
            modifier = modifier
        )
    }
}
