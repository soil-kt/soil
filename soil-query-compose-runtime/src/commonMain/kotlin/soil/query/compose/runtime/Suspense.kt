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

    fun isAwaited(): Boolean {
        return hostMap.any { it.value }
    }
}
