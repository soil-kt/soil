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
import androidx.compose.ui.geometry.center
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import soil.form.compose.FormFieldControl
import soil.form.compose.onFocusChanged
import soil.playground.style.AppTheme
import kotlin.enums.enumEntries

@Composable
fun <T : Any> FormFieldControl<T?>.RadioGroup(
    options: Iterable<T>,
    transform: (T) -> String,
    modifier: Modifier = Modifier,
    selected: (T) -> Boolean = { it == value },
    onSelect: (T) -> Unit = this::onValueChange,
    enabled: Boolean = !isDisabled,
) {
    Column {
        Row(
            modifier = modifier
                .selectableGroup()
                .onFocusChanged(this@RadioGroup),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val focusedColor = AppTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            options.forEach { option ->
                key(option) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    val isSelected = selected(option)
                    Row(
                        modifier = Modifier.selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(option) },
                            enabled = enabled
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
                            enabled = enabled
                        )
                    }
                }
            }
        }
        ErrorMessage(errors = errors, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
inline fun <reified T : Enum<T>> FormFieldControl<T?>.RadioGroup(
    modifier: Modifier = Modifier,
    noinline transform: (T) -> String = { it.name },
    noinline selected: (T) -> Boolean = { it == value },
    noinline onSelect: (T) -> Unit = this::onValueChange,
    enabled: Boolean = !isDisabled
) {
    RadioGroup(
        options = enumEntries<T>(),
        transform = transform,
        modifier = modifier,
        selected = selected,
        onSelect = onSelect,
        enabled = enabled
    )
}
