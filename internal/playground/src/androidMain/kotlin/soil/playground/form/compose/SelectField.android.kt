package soil.playground.form.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import soil.form.FieldValidator
import soil.form.compose.tooling.PreviewField
import soil.form.rule.notNull
import soil.playground.form.Title
import soil.playground.style.AppTheme

@Preview(showBackground = true)
@Composable
private fun SelectFieldPreview() {
    AppTheme {
        PreviewField<Title?>(
            initialValue = null,
            validator = FieldValidator {
                notNull { "This field is required" }
            },
            render = { field ->
                FieldLayout(field) {
                    SelectField(transform = { it?.name ?: "-" }) {
                        Title.entries.forEach { value ->
                            Option(value) {
                                Text(text = value.name)
                            }
                        }
                    }
                }
            }
        )
    }
}
