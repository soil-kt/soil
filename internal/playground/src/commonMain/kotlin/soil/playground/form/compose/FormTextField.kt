package soil.playground.form.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import soil.playground.style.withAppTheme

@Composable
fun FormTextField(
    binding: InputBinding
) = withAppTheme {
    OutlinedTextField(
        value = binding.value,
        onValueChange = binding.onValueChange,
        placeholder = { Text(binding.name) },
        modifier = binding.modifier.fillMaxWidth(),
        enabled = binding.enabled,
        isError = binding.hasError,
        singleLine = binding.singleLine,
        supportingText = {
            if (binding.hasError) {
                Text(text = binding.errors.first(), color = colorScheme.error)
            }
        },
        keyboardOptions = binding.keyboardOptions,
        visualTransformation = binding.visualTransformation
    )
}
