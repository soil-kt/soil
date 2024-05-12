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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
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
    val isAwaited by remember(state) {
        derivedStateOf { state.isAwaited() }
    }
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
    private val hostMap = mutableStateMapOf<Any, Boolean>()

    override val keys: Set<Any> get() = hostMap.keys

    override fun get(key: Any): Boolean {
        return hostMap[key] ?: false
    }

    override fun set(key: Any, isAwaited: Boolean) {
        hostMap[key] = isAwaited
    }

    override fun remove(key: Any) {
        hostMap.remove(key)
    }

    /**
     * Returns `true` if any of the [Await] is awaited.
     */
    fun isAwaited(): Boolean {
        return hostMap.any { it.value }
    }
}
