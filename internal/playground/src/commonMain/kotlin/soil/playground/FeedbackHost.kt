package soil.playground

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.coroutines.cancellation.CancellationException

@Stable
interface FeedbackHost {
    suspend fun showAlert(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult

    companion object Noop : FeedbackHost {
        override suspend fun showAlert(
            message: String,
            actionLabel: String?,
            withDismissAction: Boolean,
            duration: SnackbarDuration
        ): SnackbarResult = throw CancellationException()
    }
}

data class FeedbackAction(
    val hostState: SnackbarHostState,
) : FeedbackHost {
    override suspend fun showAlert(
        message: String,
        actionLabel: String?,
        withDismissAction: Boolean,
        duration: SnackbarDuration
    ): SnackbarResult = hostState.showSnackbar(message, actionLabel, withDismissAction, duration)
}

val LocalFeedbackHost = staticCompositionLocalOf<FeedbackHost> {
    FeedbackHost.Noop
}
