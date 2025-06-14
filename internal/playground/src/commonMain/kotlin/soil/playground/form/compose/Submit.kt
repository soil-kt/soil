package soil.playground.form.compose

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.form.compose.Form
import soil.playground.style.withAppTheme

@Composable
fun Form<*>.Submit(
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
    content: @Composable () -> Unit
) = withAppTheme {
    val btn = remember { MutableInteractionSource() }
    Button(
        onClick = ::handleSubmit,
        modifier = modifier.focusable(interactionSource = btn),
        enabled = state.meta.canSubmit,
        interactionSource = btn
    ) {
        Box(contentAlignment = Alignment.Center) {
            val contentColor = LocalContentColor.current
            val color = if (isSubmitting) {
                contentColor.copy(alpha = 0.38f)
            } else {
                contentColor
            }
            CompositionLocalProvider(LocalContentColor provides color) {
                content()
            }
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = contentColor, modifier = Modifier.progressSemantics().size(20.dp)
                )
            }
        }
    }
}
