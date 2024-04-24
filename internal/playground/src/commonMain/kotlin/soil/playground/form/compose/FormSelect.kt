package soil.playground.form.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import soil.playground.style.withAppTheme
import kotlin.enums.enumEntries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FormSelect(
    binding: SelectBinding<T?>,
    options: Iterable<T>,
    displayText: (T) -> String,
) = withAppTheme {
    ExposedDropdownMenuBox(
        expanded = binding.expanded,
        onExpandedChange = binding.onExpandedChange,
    ) {
        OutlinedTextField(
            value = binding.value?.let(displayText) ?: "",
            onValueChange = { },
            modifier = binding.modifier.fillMaxWidth().menuAnchor(),
            isError = binding.hasError,
            enabled = binding.enabled,
            readOnly = true,
            supportingText = {
                if (binding.hasError) {
                    Text(text = binding.errors.first(), color = colorScheme.error)
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = binding.expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = binding.expanded,
            onDismissRequest = binding.onDismissRequest
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(text = displayText(it)) },
                    onClick = {
                        binding.onSelect(it)
                    })
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
inline fun <reified T : Enum<T>> FormSelect(
    binding: SelectBinding<T?>,
    noinline displayText: (T) -> String = { it.name }
) {
    FormSelect(binding, enumEntries<T>(), displayText)
}
