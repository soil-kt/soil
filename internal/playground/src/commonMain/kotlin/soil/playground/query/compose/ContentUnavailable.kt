package soil.playground.query.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.client.plugins.ResponseException
import soil.playground.style.withAppTheme

@Composable
fun ContentUnavailable(
    description: String,
    modifier: Modifier = Modifier,
    title: @Composable (ColumnScope.() -> Unit)? = null,
    actions: @Composable (ColumnScope.() -> Unit)? = null,
) = withAppTheme {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title != null) {
                ProvideTextStyle(
                    value = typography.headlineSmall.merge(
                        TextStyle(
                            color = colorScheme.onSurface
                        )
                    )
                ) {
                    title()
                }
            }
            ProvideTextStyle(
                value = typography.bodyMedium.merge(
                    TextStyle(
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                )
            ) {
                Text(description)
            }
            if (actions != null) {
                ProvideTextStyle(
                    value = typography.labelMedium
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun ContentUnavailable(
    error: Throwable,
    modifier: Modifier = Modifier,
    reset: (() -> Unit)? = null,
) = withAppTheme {
    when (error) {
        is ResponseException -> {
            ContentUnavailable(
                description = error.response.status.description,
                modifier = modifier,
                title = {
                    Text(error.response.status.value.toString())
                },
                actions = {
                    if (error.response.status.value >= 500 && reset != null) {
                        OutlinedButton(onClick = reset) {
                            Text("Retry")
                        }
                    }
                }
            )
        }

        else -> {
            ContentUnavailable(
                description = "Unexpected error occurred.",
                modifier = modifier,
                actions = {
                    if (reset != null) {
                        OutlinedButton(onClick = reset) {
                            Text("Retry")
                        }
                    }
                }
            )
        }
    }
}
