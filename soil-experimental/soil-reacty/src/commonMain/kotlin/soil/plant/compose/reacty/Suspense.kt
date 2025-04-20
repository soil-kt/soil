// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Suspense render a fallback UI while some asynchronous work is being done.
 *
 * Typically, this function is defined at least once at the top level of a screen and used for initial loading.
 * Suspense can be nested as needed, which is useful for performing partial loading.
 *
 * Usage:
 *
 * ```kotlin
 * Suspense(
 *     fallback = { ContentLoading(modifier = Modifier.matchParentSize()) },
 *     modifier = Modifier.fillMaxSize()
 * ) {
 *     val query = rememberGetPostsQuery()
 *     Await(query) { posts ->
 *         PostList(posts)
 *     }
 * }
 * ```
 *
 * @param fallback The fallback UI to render components like loading.
 * @param modifier The modifier to be applied to the layout.
 * @param state The [SuspenseState] to manage the suspense.
 * @param contentThreshold The duration after which the initial content load is considered complete.
 * @param content The content to display when the suspense is not awaited.
 * @see Await
 */
@Composable
fun Suspense(
    fallback: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: SuspenseState = remember { SuspenseState() },
    contentThreshold: Duration = 3.seconds,
    content: @Composable () -> Unit
) {
    val isAwaited by state.isAwaited.collectAsState()
    var isFirstTime by remember { mutableStateOf(true) }
    Box(modifier = modifier) {
        CompositionLocalProvider(LocalAwaitHost provides state) {
            ContentVisibility(hidden = isAwaited && isFirstTime, content = content)
        }
        if (isAwaited) {
            fallback()
        }
    }
    LaunchedEffect(isAwaited) {
        if (!isAwaited && isFirstTime) {
            delay(contentThreshold)
            isFirstTime = false
        }
    }
}

/**
 * State of the [Suspense].
 */
@Stable
class SuspenseState : AwaitHost {
    private val hostMap = mutableMapOf<Any, Boolean>()

    // FIXME: This can be fixed by enabling K2 mode
    private val _isAwaited = MutableStateFlow(false)
    val isAwaited: StateFlow<Boolean> = _isAwaited

    override val keys: Set<Any> get() = hostMap.keys

    override fun get(key: Any): Boolean {
        return hostMap[key] ?: false
    }

    override fun set(key: Any, isAwaited: Boolean) {
        hostMap[key] = isAwaited
        onStateChanged()
    }

    override fun remove(key: Any) {
        hostMap.remove(key)
        onStateChanged()
    }

    private fun onStateChanged() {
        _isAwaited.value = hostMap.any { it.value }
    }
}
