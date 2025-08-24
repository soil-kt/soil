package soil.playground.form.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import soil.form.compose.BasicFormField
import soil.form.compose.hasError
import soil.playground.style.withAppTheme

@Composable
inline fun <V : BasicFormField> V.WithLayout(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    content: @Composable V.() -> Unit
) = withAppTheme {
    Column(
        modifier = modifier
            .ifTrue(!LocalInspectionMode.current) {
                animateContentSize()
            },
        verticalArrangement = verticalArrangement
    ) {
        content()
        if (isEnabled && hasError) {
            FieldValidationError(
                text = error.messages.first(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
