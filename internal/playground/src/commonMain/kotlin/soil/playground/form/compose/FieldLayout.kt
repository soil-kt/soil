package soil.playground.form.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.form.compose.FormField
import soil.form.compose.hasError
import soil.playground.style.withAppTheme

@Composable
fun <V> FieldLayout(
    field: FormField<V>,
    modifier: Modifier = Modifier,
    content: @Composable FormField<V>.() -> Unit
) = withAppTheme {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        field.content()
        if (field.isEnabled && field.hasError) {
            FieldValidationError(
                text = field.error.messages.first(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
