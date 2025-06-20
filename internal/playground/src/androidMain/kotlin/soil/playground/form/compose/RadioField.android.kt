package soil.playground.form.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import soil.form.FieldValidator
import soil.form.compose.tooling.PreviewField
import soil.form.rule.notNull
import soil.playground.style.AppTheme

@Preview(showBackground = true)
@Composable
private fun RadioFieldPreview() {
    AppTheme {
        PreviewField<Boolean?>(
            initialValue = null,
            validator = FieldValidator {
                notNull { "This field is required" }
            },
            render = { field ->
                FieldLayout(field) {
                    RadioField {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Option(true) {
                                Text(text = "Yes")
                            }
                            Option(false) {
                                Text(text = "No")
                            }
                        }
                    }
                }
            }
        )
    }
}
