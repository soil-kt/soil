package soil.playground.form.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.invisibleToUser
import soil.form.FieldErrors
import soil.playground.style.withAppTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ErrorMessage(
    errors: FieldErrors,
    modifier: Modifier = Modifier
) = withAppTheme {
    val hasError = errors.isNotEmpty()
    Box(
        modifier = modifier
            .alpha(if (!hasError) 0f else 1f)
            .clearAndSetSemantics {
                if (hasError) {
                    error(errors.first())
                } else {
                    invisibleToUser()
                }
            }
    ) {
        Text(
            text = if (hasError) errors.first() else "",
            style = typography.bodySmall,
            color = colorScheme.error
        )
    }
}
