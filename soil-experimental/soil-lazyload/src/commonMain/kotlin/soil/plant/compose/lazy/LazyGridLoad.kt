// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.lazy

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A composable function that enables infinite scrolling for a LazyVerticalGrid or LazyHorizontalGrid.
 *
 * This function automatically triggers the [loadMore] callback when the user scrolls
 * close to the end of the grid, based on the specified [threshold].
 *
 * **NOTE:** For each unique [loadMoreParam] value, the [loadMore] callback will be invoked at most once.
 * When [loadMoreParam] changes, the load detection will start over with the new parameter.
 *
 * @param T The type of parameter to pass to the [loadMore] callback
 * @param state The [LazyGridState] of the lazy grid to monitor
 * @param loadMore Callback function that will be invoked to load additional data
 * @param loadMoreParam Parameter to pass to the [loadMore] callback, if null no loading will occur
 * @param threshold Configuration that determines when to trigger loading more items
 * @param debounceTimeout Duration to debounce scroll events to prevent multiple rapid load requests
 */
@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
@Composable
inline fun <T : Any> LazyLoad(
    state: LazyGridState,
    loadMore: LazyLoadMore<T>,
    loadMoreParam: T?,
    threshold: LazyGridThreshold = LazyGridThreshold(),
    debounceTimeout: Duration = 200.milliseconds
) {
    LazyLoad(
        state = state,
        loadMore = loadMore,
        loadMoreParam = loadMoreParam,
        strategy = remember { defaultLazyGridStrategy(threshold) },
        debounceTimeout = debounceTimeout
    )
}

/**
 * Configuration class for determining when to load more items in a LazyVerticalGrid or LazyHorizontalGrid.
 *
 * Loading more items is triggered when both of the following conditions are met:
 * - The number of remaining items is less than or equal to [remainingItems]
 * - The ratio of remaining items to total items is less than or equal to [remainingRatio]
 *
 * @property remainingItems Threshold for the number of remaining items before triggering load more
 * @property remainingRatio Threshold for the ratio of remaining items to total items (0.2f = 20%)
 */
class LazyGridThreshold(
    val remainingItems: Int = 6,
    val remainingRatio: Float = 0.2f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LazyGridThreshold

        if (remainingItems != other.remainingItems) return false
        if (remainingRatio != other.remainingRatio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remainingItems
        result = 31 * result + remainingRatio.hashCode()
        return result
    }
}

/**
 * Creates a default [LazyLoadStrategy] for lazy grids based on the specified [threshold].
 *
 * The strategy evaluates the current state of the lazy grid and determines if more items
 * should be loaded based on the remaining items count and ratio.
 *
 * @param threshold Configuration that determines when to trigger loading more items
 * @return A [LazyLoadStrategy] for lazy grids
 */
@PublishedApi
internal fun defaultLazyGridStrategy(threshold: LazyGridThreshold) = LazyLoadStrategy<LazyGridState> { state ->
    val layoutInfo = state.layoutInfo
    val totalItemsNumber = layoutInfo.totalItemsCount
    val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
    val remainingItems = totalItemsNumber - lastVisibleItemIndex
    val remainingRatio = if (totalItemsNumber > 0) {
        remainingItems.toFloat() / totalItemsNumber.toFloat()
    } else {
        0f
    }
    remainingItems <= threshold.remainingItems &&
        remainingRatio <= threshold.remainingRatio
}
