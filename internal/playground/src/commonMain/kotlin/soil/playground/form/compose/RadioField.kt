package soil.playground.form.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.center
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import soil.form.compose.FormField
import soil.playground.style.AppTheme

@Composable
fun <T : Any> RadioField(
    ref: FormField<T?>,
    modifier: Modifier = Modifier,
    content: @Composable RadioFieldScope<T>.() -> Unit
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .onFocusChanged { ref.handleFocus(it.isFocused || it.hasFocus) },
    ) {
        val scope = remember(ref) { RadioFieldScope(ref) }
        scope.content()
        FieldInfo(ref, modifier = Modifier.padding(top = 4.dp))
    }
}

@Stable
class RadioFieldScope<T : Any>(
    private val ref: FormField<T?>
) {

    @Composable
    fun Option(
        value: T,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        val isSelected = value == ref.value
        val focusedColor = AppTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        Row(
            modifier = modifier.selectable(
                selected = isSelected,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = { ref.onValueChange(value) },
                enabled = ref.isEnabled
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
            RadioButton(
                selected = isSelected,
                onClick = null,
                modifier = Modifier.ifTrue(isFocused) {
                    Modifier.drawBehind {
                        drawCircle(
                            focusedColor,
                            radius = size.maxDimension * 0.75f,
                            center = size.center
                        )
                    }
                },
                interactionSource = interactionSource,
                enabled = ref.isEnabled
            )
        }
    }
}
