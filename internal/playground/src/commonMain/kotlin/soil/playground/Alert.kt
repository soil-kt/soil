package soil.playground

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun Alert(
    message: String,
    actionLabel: String? = null,
    longish: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    onActionPerformed: (() -> Unit)? = null,
    host: FeedbackHost = LocalFeedbackHost.current
) {
    val onDismissCallback by rememberUpdatedState(newValue = onDismiss)
    val onActionPerformedCallback by rememberUpdatedState(newValue = onActionPerformed)
    LaunchedEffect(message) {
        val result = host.showAlert(
            message = message,
            actionLabel = actionLabel,
            duration = if (longish) SnackbarDuration.Long else SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.Dismissed -> onDismissCallback?.invoke()
            SnackbarResult.ActionPerformed -> onActionPerformedCallback?.invoke()
        }
    }
}
