// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("unused")

package soil.query.compose.runtime

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlin.jvm.JvmInline
import kotlin.DeprecationLevel
import kotlin.ReplaceWith

/**
 * Provides a [LaunchedEffect] to perform additional loading for [soil.query.compose.InfiniteQueryObject].
 *
 * The percentage is calculated from the values of [computeVisibleItemIndex] and [totalItemsCount].
 * It only triggers when the [threshold] is reached.
 *
 * @param state The scroll state of a [androidx.compose.foundation.lazy.LazyColumn] or [androidx.compose.foundation.lazy.LazyRow].
 * @param loadMore Specifies the function to call for loading more items, typically [soil.query.compose.InfiniteQueryObject.loadMore].
 * @param loadMoreParam The parameter passed to [soil.query.compose.InfiniteQueryObject.loadMore].  If `null`, the effect is not triggered.
 * @param totalItemsCount The total number of items related to [state].
 * @param threshold The threshold scroll position at which to trigger additional loading.
 * @param direction The direction for loading more items.
 * @param computeVisibleItemIndex A function that calculates the index of the visible item used as the reference for additional loading.
 */
@Composable
@Deprecated(
    message = "This implementation is deprecated. Please use the new LazyLoad from soil-experimental:soil-lazyload module instead.",
    replaceWith = ReplaceWith(
        "LazyLoad(state, loadMore, loadMoreParam)",
        "soil.plant.compose.lazy.LazyLoad"
    ),
    level = DeprecationLevel.WARNING
)
inline fun <T : Any> LazyLoadEffect(
    state: LazyListState,
    noinline loadMore: suspend (T) -> Unit,
    loadMoreParam: T?,
    totalItemsCount: Int,
    threshold: LazyLoadThreshold = LazyLoadThreshold.Lazily,
    direction: LazyLoadDirection = LazyLoadDirection.Forward,
    crossinline computeVisibleItemIndex: (state: LazyListState) -> Int = {
        it.firstVisibleItemIndex + it.layoutInfo.visibleItemsInfo.size / 2
    }
) {
    LazyLoadCustomEffect(
        state = state,
        loadMore = loadMore,
        loadMoreParam = loadMoreParam,
        totalItemsCount = totalItemsCount,
        threshold = threshold,
        direction = direction,
        computeVisibleItemIndex = computeVisibleItemIndex
    )
}

/**
 * Provides a [LaunchedEffect] to perform additional loading for [soil.query.compose.InfiniteQueryObject].
 *
 * The percentage is calculated from the values of [computeVisibleItemIndex] and [totalItemsCount].
 * It only triggers when the [threshold] is reached.
 *
 * @param state The scroll state of a [androidx.compose.foundation.lazy.grid.LazyGrid].
 * @param loadMore Specifies the function to call for loading more items, typically [soil.query.compose.InfiniteQueryObject.loadMore].
 * @param loadMoreParam The parameter passed to [soil.query.compose.InfiniteQueryObject.loadMore].  If `null`, the effect is not triggered.
 * @param totalItemsCount The total number of items related to [state].
 * @param threshold The threshold scroll position at which to trigger additional loading.
 * @param direction The direction for loading more items.
 * @param computeVisibleItemIndex A function that calculates the index of the visible item used as the reference for additional loading.
 */
@Composable
@Deprecated(
    message = "This implementation is deprecated. Please use the new LazyLoad from soil-experimental:soil-lazyload module instead.",
    replaceWith = ReplaceWith(
        "LazyLoad(state, loadMore, loadMoreParam)",
        "soil.plant.compose.lazy.LazyLoad"
    ),
    level = DeprecationLevel.WARNING
)
inline fun <T : Any> LazyLoadEffect(
    state: LazyGridState,
    noinline loadMore: suspend (T) -> Unit,
    loadMoreParam: T?,
    totalItemsCount: Int,
    threshold: LazyLoadThreshold = LazyLoadThreshold.Lazily,
    direction: LazyLoadDirection = LazyLoadDirection.Forward,
    crossinline computeVisibleItemIndex: (state: LazyGridState) -> Int = {
        it.firstVisibleItemIndex + it.layoutInfo.visibleItemsInfo.size / 2
    }
) {
    LazyLoadCustomEffect(
        state = state,
        loadMore = loadMore,
        loadMoreParam = loadMoreParam,
        totalItemsCount = totalItemsCount,
        threshold = threshold,
        direction = direction,
        computeVisibleItemIndex = computeVisibleItemIndex
    )
}

/**
 * Provides a [LaunchedEffect] to perform additional loading for [soil.query.compose.InfiniteQueryObject].
 *
 * The percentage is calculated from the values of [computeVisibleItemIndex] and [totalItemsCount].
 * It only triggers when the [threshold] is reached.
 *
 * @param state The scroll state inherited from [ScrollableState].
 * @param loadMore Specifies the function to call for loading more items, typically [soil.query.compose.InfiniteQueryObject.loadMore].
 * @param loadMoreParam The parameter passed to [soil.query.compose.InfiniteQueryObject.loadMore].  If `null`, the effect is not triggered.
 * @param totalItemsCount The total number of items related to [state].
 * @param threshold The threshold scroll position at which to trigger additional loading.
 * @param direction The direction for loading more items.
 * @param computeVisibleItemIndex A function that calculates the index of the visible item used as the reference for additional loading.
 */
@Composable
inline fun <T : Any, S : ScrollableState> LazyLoadCustomEffect(
    state: S,
    noinline loadMore: suspend (T) -> Unit,
    loadMoreParam: T?,
    totalItemsCount: Int,
    threshold: LazyLoadThreshold = LazyLoadThreshold.Lazily,
    direction: LazyLoadDirection = LazyLoadDirection.Forward,
    crossinline computeVisibleItemIndex: (state: S) -> Int
) {
    LaunchedEffect(state, totalItemsCount, loadMore, loadMoreParam) {
        if (totalItemsCount == 0 || loadMoreParam == null) return@LaunchedEffect
        snapshotFlow {
            val itemIndex = computeVisibleItemIndex(state).coerceIn(0, totalItemsCount - 1)
            itemIndex to direction.canScrollMore(state)
        }
            .filter { (itemIndex, canScrollMore) ->
                !canScrollMore || direction.getScrollPositionRatio(itemIndex, totalItemsCount) >= threshold.value
            }
            .take(1)
            .collect {
                loadMore(loadMoreParam)
            }
    }
}

@JvmInline
value class LazyLoadThreshold(val value: Float) {
    init {
        require(value in 0.0f..1.0f) { "Threshold value must be in the range [0.0, 1.0]" }
    }

    companion object {
        val Eagerly = LazyLoadThreshold(0.5f)
        val Lazily = LazyLoadThreshold(0.75f)
    }
}

sealed interface LazyLoadDirection {
    fun canScrollMore(state: ScrollableState): Boolean
    fun getScrollPositionRatio(itemIndex: Int, totalItemsCount: Int): Float

    data object Backward : LazyLoadDirection {
        override fun canScrollMore(state: ScrollableState): Boolean = state.canScrollBackward
        override fun getScrollPositionRatio(itemIndex: Int, totalItemsCount: Int): Float {
            return 1f - (1f * itemIndex / totalItemsCount)
        }
    }

    data object Forward : LazyLoadDirection {
        override fun canScrollMore(state: ScrollableState): Boolean = state.canScrollForward
        override fun getScrollPositionRatio(itemIndex: Int, totalItemsCount: Int): Float {
            return 1f * (itemIndex + 1) / totalItemsCount
        }
    }
}
