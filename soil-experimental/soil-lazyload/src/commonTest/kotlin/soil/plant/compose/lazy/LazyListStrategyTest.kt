// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.lazy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LazyListStrategyTest : UnitTest() {

    @Test
    fun testDefaultLazyListStrategy_emptyList_returnsFalse() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(LazyListThreshold())
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                // Empty list
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        assertTrue { result == false }
    }

    @Test
    fun testDefaultLazyListStrategy_farFromEnd_returnsFalse() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(LazyListThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(30) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // In initial state, only first few items are visible, so it returns false
        assertTrue { result == false }
    }

    @Test
    fun testDefaultLazyListStrategy_nearEnd_returnsTrue() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(LazyListThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState(
                // Start near the end
                initialFirstVisibleItemIndex = 24
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(30) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when remaining items are 6 or less
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyListStrategy_atEnd_returnsTrue() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(LazyListThreshold())
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState(
                // Start from the last item
                initialFirstVisibleItemIndex = 29
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(30) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when the last item is visible
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyListStrategy_smallList_allVisible() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(LazyListThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.height(500.dp)
            ) {
                // Small list where all items are visible
                items(5) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when all items are visible (0 remaining)
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyListStrategy_customThreshold_zeroItems() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(
            LazyListThreshold(remainingItems = 0)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState(
                initialFirstVisibleItemIndex = 29
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(30) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true only when exactly 0 items remain
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyListStrategy_highRatio() = runComposeUiTest {
        val strategy = defaultLazyListStrategy(
            LazyListThreshold(remainingItems = 10)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(10) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when remaining items condition is met
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyListStrategy_largeThreshold_returnsFalse() = runComposeUiTest {
        // Small threshold that won't be met in middle position
        val strategy = defaultLazyListStrategy(
            LazyListThreshold(remainingItems = 3)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState(
                initialFirstVisibleItemIndex = 50
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(100) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns false when threshold is not met (50 remaining > 3)
        assertTrue { result == false }
    }

    @Test
    fun testDefaultLazyListStrategy_smallThreshold_returnsTrue() = runComposeUiTest {
        // Small threshold with position near end
        val strategy = defaultLazyListStrategy(
            LazyListThreshold(remainingItems = 5)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyListState(
                initialFirstVisibleItemIndex = 95
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(100) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when remaining items <= threshold
        assertTrue { result == true }
    }
}
