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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
fun <T : Any> FormField<T?>.RadioGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .onFocusChanged { handleFocus(it.isFocused || it.hasFocus) },
    ) {
        content()
        FieldInfo(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun <T : Any> FormField<T?>.RadioItem(
    option: T,
    transform: (T) -> String,
    modifier: Modifier = Modifier
) {
    key(option) {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        val isSelected = option == value
        val focusedColor = AppTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        Row(
            modifier = modifier.selectable(
                selected = isSelected,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = { onValueChange(option) },
                enabled = isEnabled
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = transform(option))
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
                enabled = isEnabled
            )
        }
    }
}
