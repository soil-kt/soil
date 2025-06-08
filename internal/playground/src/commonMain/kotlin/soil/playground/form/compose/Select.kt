package soil.playground.form.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import soil.form.compose.FormField
import soil.playground.style.AppTheme
import kotlin.enums.enumEntries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormField<T?>.Select(
    options: Iterable<T>,
    transform: (T?) -> String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    },
    supportingText: @Composable (() -> Unit)? = null,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        OutlinedTextField(
            value = transform(value),
            onValueChange = { /* no-op: read-only */ },
            modifier = modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, isEnabled)
                .onFocusChanged { handleFocus(it.isFocused || it.hasFocus) },
            isError = hasError,
            enabled = isEnabled,
            readOnly = true,
            supportingText = supportingText ?: {
                if (hasError) {
                    Text(text = error.messages.first(), color = AppTheme.colorScheme.error)
                }
            },
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                key(option) {
                    DropdownMenuItem(
                        text = { Text(text = transform(option)) },
                        onClick = {
                            onValueChange(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <T : Any> FormField<T?>.Select(
    options: Iterable<T>,
    noinline transform: (T?) -> String,
    modifier: Modifier = Modifier,
    noinline label: @Composable (() -> Unit)? = null,
    noinline placeholder: @Composable (() -> Unit)? = null,
    noinline leadingIcon: @Composable (() -> Unit)? = null,
    noinline supportingText: @Composable (() -> Unit)? = null,
) {
    var isExpanded by remember { mutableStateOf(false) }
    Select(
        options = options,
        transform = transform,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier.onFocusEvent { state ->
            if (state.isFocused && value == null) {
                isExpanded = true
            }
        },
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
        },
        supportingText = supportingText
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : Enum<T>> FormField<T?>.Select(
    expanded: Boolean,
    noinline onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    noinline label: @Composable (() -> Unit)? = null,
    noinline placeholder: @Composable (() -> Unit)? = null,
    noinline leadingIcon: @Composable (() -> Unit)? = null,
    noinline trailingIcon: @Composable (() -> Unit)? = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
    },
    noinline supportingText: @Composable (() -> Unit)? = null,
    noinline transform: (T?) -> String = { it?.name ?: "" },
) {
    Select(
        options = enumEntries<T>(),
        transform = transform,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : Enum<T>> FormField<T?>.Select(
    modifier: Modifier = Modifier,
    noinline label: @Composable (() -> Unit)? = null,
    noinline placeholder: @Composable (() -> Unit)? = null,
    noinline leadingIcon: @Composable (() -> Unit)? = null,
    noinline supportingText: @Composable (() -> Unit)? = null,
    noinline transform: (T?) -> String = { it?.name ?: "" },
) {
    var isExpanded by remember { mutableStateOf(false) }
    Select(
        options = enumEntries<T>(),
        transform = transform,
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier.onFocusEvent { state ->
            if (state.isFocused && value == null) {
                isExpanded = true
            }
        },
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
        },
        supportingText = supportingText
    )
}
