// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.lazy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Core composable that implements infinite scrolling logic for any scrollable state.
 *
 * This function sets up a [LaunchedEffect] that monitors the scroll state and triggers
 * the [loadMore] callback when the scrolling position meets the conditions defined by the [strategy].
 *
 * **NOTE**: For each unique [loadMoreParam] value, the [loadMore] callback will be invoked at most once.
 * When [loadMoreParam] changes, the load detection will start over with the new parameter.
 *
 * @param T The type of parameter to pass to the [loadMore] callback
 * @param S The type of scrollable state being monitored
 * @param state The scrollable state to monitor (e.g., LazyListState, LazyGridState)
 * @param loadMore Callback function that will be invoked to load additional data
 * @param loadMoreParam Parameter to pass to the [loadMore] callback, if null no loading will occur
 * @param strategy Strategy that determines when additional data should be loaded
 * @param debounceTimeout Duration to debounce scroll events to prevent multiple rapid load requests
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
@OptIn(FlowPreview::class)
@Composable
inline fun <T : Any, S> LazyLoad(
    state: S,
    loadMore: LazyLoadMore<T>,
    loadMoreParam: T?,
    strategy: LazyLoadStrategy<S>,
    debounceTimeout: Duration = 200.milliseconds
) {
    LaunchedEffect(state, loadMoreParam) {
        if (loadMoreParam == null) {
            return@LaunchedEffect
        }
        snapshotFlow { strategy.shouldLoadMore(state) }
            .debounce(debounceTimeout)
            .filter { it }
            .take(1)
            .collect {
                loadMore(loadMoreParam)
            }
    }
}

/**
 * Interface that defines the strategy for determining when to load more items.
 *
 * Implement this interface to customize when the infinite scrolling should trigger
 * the load more callback based on the current scroll state.
 *
 * @param T The type of scrollable state to evaluate
 */
@Stable
fun interface LazyLoadStrategy<T> {
    /**
     * Determines if more data should be loaded based on the current scroll state.
     *
     * @param state The current scroll state to evaluate
     * @return True if more data should be loaded, false otherwise
     */
    fun shouldLoadMore(state: T): Boolean
}

/**
 * Interface that defines the callback for loading more items.
 *
 * This callback is triggered when the scrolling position meets the conditions
 * defined by the [LazyLoadStrategy].
 *
 * @param T The type of parameter that will be passed to the callback
 */
@Stable
fun interface LazyLoadMore<T> {
    /**
     * Invokes the load more operation with the specified parameter.
     *
     * @param param The parameter to use for loading more data
     */
    suspend operator fun invoke(param: T)
}
