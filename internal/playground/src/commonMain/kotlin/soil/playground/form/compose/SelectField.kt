package soil.playground.form.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import soil.form.compose.FormFieldControl
import soil.form.compose.hasError
import soil.playground.style.AppTheme
import kotlin.enums.enumEntries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormFieldControl<T?>.SelectField(
    options: Iterable<T>,
    transform: (T?) -> String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = isEnabled,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    },
    supportingText: @Composable (() -> Unit)? = {
        if (hasError) {
            Text(text = error.messages.first(), color = AppTheme.colorScheme.error)
        }
    },
    isError: Boolean = hasError,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            value = transform(value),
            onValueChange = { /* no-op: read-only */ },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled)
                .onFocusChanged { handleFocus(it.isFocused || it.hasFocus) },
            isError = isError,
            enabled = enabled,
            readOnly = true,
            supportingText = supportingText,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(text = transform(it)) },
                    onClick = {
                        onValueChange(it)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : Enum<T>> FormFieldControl<T?>.SelectField(
    expanded: Boolean,
    noinline onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = isEnabled,
    noinline label: @Composable (() -> Unit)? = null,
    noinline placeholder: @Composable (() -> Unit)? = null,
    noinline leadingIcon: @Composable (() -> Unit)? = null,
    noinline trailingIcon: @Composable (() -> Unit)? = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    },
    noinline supportingText: @Composable (() -> Unit)? = {
        if (hasError) {
            Text(text = error.messages.first(), color = AppTheme.colorScheme.error)
        }
    },
    isError: Boolean = hasError,
    noinline transform: (T?) -> String = { it?.name ?: "" },
) {
    SelectField(
        options = enumEntries<T>(),
        transform = transform,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        enabled = enabled,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError
    )
}
