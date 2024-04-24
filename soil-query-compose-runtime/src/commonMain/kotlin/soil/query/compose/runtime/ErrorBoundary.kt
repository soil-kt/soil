// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    fallback: (@Composable BoxScope.(ctx: ErrorBoundaryContext) -> Unit)? = null,
    onError: ((err: Throwable) -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    state: ErrorBoundaryState = remember { ErrorBoundaryState() },
    content: @Composable () -> Unit
) {
    val currentError by remember(state) { derivedStateOf { state.error } }
    val onErrorCallback by rememberUpdatedState(newValue = onError)
    val onResetCallback by rememberUpdatedState(newValue = onReset)
    Box(modifier) {
        CompositionLocalProvider(
            LocalCatchThrowHost provides state
        ) {
            ContentVisibility(
                hidden = currentError != null && fallback != null,
                content = content
            )
        }
        val err = currentError
        if (err != null) {
            val ctx = remember(err, onResetCallback) {
                ErrorBoundaryContext(err, onResetCallback)
            }
            if (fallback != null) {
                fallback(ctx)
            }
            LaunchedEffect(err) {
                onErrorCallback?.invoke(err)
            }
        }
    }
}

@Stable
class ErrorBoundaryContext(
    val err: Throwable,
    val reset: (() -> Unit)?
)

@Stable
class ErrorBoundaryState : CatchThrowHost {
    private val hostMap = mutableStateMapOf<Any, Throwable>()

    val error: Throwable?
        get() = hostMap.values.firstOrNull()

    override val keys: Set<Any> get() = hostMap.keys

    override fun get(key: Any): Throwable? {
        return hostMap[key]
    }

    override fun set(key: Any, error: Throwable) {
        hostMap[key] = error
    }

    override fun remove(key: Any) {
        hostMap.remove(key)
    }
}
