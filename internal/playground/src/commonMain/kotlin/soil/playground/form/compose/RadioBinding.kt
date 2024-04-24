package soil.playground.form.compose

import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import soil.form.Field
import soil.form.FieldErrors
import soil.form.compose.onFocusChanged

@Stable
class RadioBinding<T>(
    private val rawField: Field<T>,
    private val rawModifier: Modifier,
    val onSelect: (T) -> Unit
) {
    val name: String get() = rawField.name
    val value: T get() = rawField.value
    val hasError: Boolean get() = rawField.hasError
    val errors: FieldErrors get() = rawField.errors
    val enabled: Boolean get() = rawField.isEnabled
    val modifier: Modifier get() = rawModifier.selectableGroup()

    fun itemModifier(
        value: T,
        enabled: Boolean = true
    ): Modifier = Modifier.selectable(
        selected = value == this.value,
        role = Role.RadioButton,
        onClick = { onSelect(value) },
        enabled = enabled && this.enabled
    )

    fun itemModifier(
        value: T,
        interactionSource: MutableInteractionSource,
        indication: Indication?,
        enabled: Boolean = true
    ): Modifier = Modifier.selectable(
        selected = value == this.value,
        interactionSource = interactionSource,
        indication = indication,
        role = Role.RadioButton,
        onClick = { onSelect(value) },
        enabled = enabled && this.enabled
    )
}

@Composable
fun <T> Field<T>.rememberAsRadio(
    focusRequester: FocusRequester? = null,
    focusNext: FocusRequester? = null,
    focusNextKeyEvent: (KeyEvent) -> Boolean = { false }
): RadioBinding<T> {
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
            handleFocusNext()
        }
    }

    return RadioBinding(
        rawField = this,
        rawModifier = modifier,
        onSelect = onSelect
    )
}
