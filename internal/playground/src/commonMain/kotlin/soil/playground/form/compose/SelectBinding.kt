package soil.playground.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import soil.form.Field
import soil.form.FieldErrors
import soil.form.compose.onFocusChanged

@Stable
class SelectBinding<T>(
    private val rawField: Field<T>,
    private val rawModifier: Modifier,
    val onSelect: (T) -> Unit,
    val expanded: Boolean,
    val onExpandedChange: (Boolean) -> Unit,
    val onDismissRequest: () -> Unit = { onExpandedChange(false) }
) {
    val name: String get() = rawField.name
    val value: T get() = rawField.value
    val hasError: Boolean get() = rawField.hasError
    val errors: FieldErrors get() = rawField.errors
    val enabled: Boolean get() = rawField.isEnabled
    val modifier: Modifier get() = rawModifier
}

@Composable
fun <T> Field<T>.rememberAsSelect(
    focusRequester: FocusRequester? = null,
    focusNext: FocusRequester? = null,
    focusNextKeyEvent: (KeyEvent) -> Boolean = { false }
): SelectBinding<T> {
    var expanded by remember(this) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val handleFocusNext = remember(focusNext) {
        { focusNext?.requestFocus() ?: focusManager.moveFocus(FocusDirection.Next) }
    }
    val handlePreviewKeyEvent = remember<(KeyEvent) -> Boolean>(handleFocusNext, focusNextKeyEvent) {
        { event ->
            if (focusNextKeyEvent(event)) {
                handleFocusNext()
                true
            } else {
                false
            }
        }
    }

    val modifier = remember(focusRequester, focusNext, handlePreviewKeyEvent) {
        Modifier
            .onFocusChanged {
                expanded = isEnabled && it.isFocused
            }
            .onFocusChanged(this)
            .onPreviewKeyEvent(handlePreviewKeyEvent)
            .ifNotNull(focusRequester) {
                focusRequester(it)
            }
            .ifNotNull(focusNext) {
                focusProperties { next = it }
            }
    }

    val onSelect = remember<(T) -> Unit>(this) {
        { value ->
            onChange(value)
            expanded = false
            handleFocusNext()
        }
    }

    return SelectBinding(
        rawField = this,
        rawModifier = modifier,
        onSelect = onSelect,
        expanded = expanded,
        onExpandedChange = { expanded = !it }
    )
}
