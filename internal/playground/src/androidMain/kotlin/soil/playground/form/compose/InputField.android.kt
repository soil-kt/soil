package soil.playground.form.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import soil.form.FieldValidator
import soil.form.compose.tooling.PreviewField
import soil.form.rule.notBlank
import soil.playground.style.AppTheme

@Preview(showBackground = true)
@Composable
private fun InputFieldPreview() {
    AppTheme {
        PreviewField(
            initialValue = "",
            validator = FieldValidator {
                notBlank { "This field cannot be blank" }
            },
            render = { field ->
                FieldLayout(field) {
                    InputField(label = { Text("Name") })
                }
            }
        )
    }
}
