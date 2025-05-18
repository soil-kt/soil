package soil.playground.form.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.invisibleToUser
import soil.form.FieldError
import soil.playground.style.withAppTheme

@Composable
fun ErrorMessage(
    error: FieldError,
    modifier: Modifier = Modifier
) = withAppTheme {
    val hasError = error.messages.isNotEmpty()
    Box(
        modifier = modifier
            .alpha(if (!hasError) 0f else 1f)
            .clearAndSetSemantics {
                if (hasError) {
                    error(error.messages.first())
                } else {
                    invisibleToUser()
                }
            }
    ) {
        Text(
            text = if (hasError) error.messages.first() else "",
            style = typography.bodySmall,
            color = colorScheme.error
        )
    }
}
