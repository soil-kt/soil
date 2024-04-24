package soil.playground.form.compose

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.center
import androidx.compose.ui.unit.dp
import soil.playground.style.withAppTheme
import kotlin.enums.enumEntries

@Composable
fun <T> FormRadioGroup(
    binding: RadioBinding<T?>,
    options: Iterable<T>,
    displayText: (T) -> String
) = withAppTheme {
    Column {
        Row(
            modifier = binding.modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val focusedColor = colorScheme.onBackground.copy(alpha = 0.1f)
            options.forEach { option ->
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                Row(
                    modifier = binding.itemModifier(
                        value = option,
                        interactionSource = interactionSource,
                        indication = null
                    ).focusable(interactionSource = interactionSource),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = displayText(option))
                    RadioButton(
                        selected = binding.value == option,
                        onClick = null,
                        modifier = Modifier.ifTrue(isFocused) {
                            Modifier.drawBehind {
                                drawCircle(
                                    focusedColor,
                                    radius = size.maxDimension * 0.75f,
                                    center = size.center
                                )
                            }
                        }, interactionSource = interactionSource
                    )
                }
            }
        }
        ErrorMessage(errors = binding.errors, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
inline fun <reified T : Enum<T>> FormRadioGroup(
    binding: RadioBinding<T?>,
    noinline displayText: (T) -> String = { it.name }
) {
    FormRadioGroup(binding, enumEntries<T>(), displayText)
}
