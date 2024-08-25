// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Wrap an ErrorBoundary around other [Catch] composable functions to catch errors and render a fallback UI.
 *
 * **Note:**
 * Typically, this function is defined at the top level of a screen and used for default error handling.
 * Do not propagate errors from ErrorBoundary to higher-level components since the error state is managed by [state].
 * Instead, it's recommended to catch and handle domain-specific exceptions within [Catch] content blocks.
 *
 * Usage:
 *
 * ```kotlin
 * ErrorBoundary(
 *     modifier = Modifier.fillMaxSize(),
 *     fallback = {
 *         ContentUnavailable(
 *             error = it.err,
 *             reset = it.reset,
 *             modifier = Modifier.matchParentSize()
 *         )
 *     },
 *     onError = { e -> println(e.toString()) },
 *     onReset = rememberQueriesErrorReset()
 * ) {
 *     Suspense(..) {
 *         val query = rememberGetPostsQuery()
 *         ..
 *         Catch(query) { e ->
 *             // You can also write your own error handling logic.
 *             if (e is DomainSpecificException) {
 *                 Alert(..)
 *                 return@Catch
 *             }
 *             Throw(e)
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier The modifier to be applied to the layout.
 * @param fallback The fallback UI to render when an error is caught.
 * @param onError The callback to be called when an error is caught.
 * @param onReset The callback to be called when the reset button is clicked.
 * @param state The state of the [ErrorBoundary].
 * @param content The content of the [ErrorBoundary].
 * @see Catch
 */
@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    fallback: (@Composable BoxScope.(ctx: ErrorBoundaryContext) -> Unit)? = null,
    onError: ((err: Throwable) -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    state: ErrorBoundaryState = remember { ErrorBoundaryState() },
    content: @Composable () -> Unit
) {
    val currentError by state.error.collectAsState()
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

/**
 * Context information to pass to the fallback UI of [ErrorBoundary].
 *
 * @property err The caught error.
 * @property reset The callback to invoke when the reset button placed within the content is clicked.
 */
@Stable
class ErrorBoundaryContext(
    val err: Throwable,
    val reset: (() -> Unit)?
)

/**
 * State of the [ErrorBoundary].
 */
@Stable
class ErrorBoundaryState : CatchThrowHost {
    private val hostMap = mutableMapOf<Any, Throwable>()

    // FIXME: This can be fixed by enabling K2 mode
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    override val keys: Set<Any> get() = hostMap.keys

    override fun get(key: Any): Throwable? {
        return hostMap[key]
    }

    override fun set(key: Any, error: Throwable) {
        hostMap[key] = error
        onStateChanged()
    }

    override fun remove(key: Any) {
        hostMap.remove(key)
        onStateChanged()
    }

    private fun onStateChanged() {
        _error.value = hostMap.values.firstOrNull()
    }
}
