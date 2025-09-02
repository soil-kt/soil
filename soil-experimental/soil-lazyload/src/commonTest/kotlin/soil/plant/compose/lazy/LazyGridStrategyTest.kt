// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.lazy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class LazyGridStrategyTest : UnitTest() {

    @Test
    fun testDefaultLazyGridStrategy_emptyGrid_returnsFalse() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold())
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                // Empty grid
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        assertTrue { result == false }
    }

    @Test
    fun testDefaultLazyGridStrategy_farFromEnd_returnsFalse() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(60) { index ->
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
    fun testDefaultLazyGridStrategy_nearEnd_returnsTrue() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState(
                // Start near the end (considering grid has 2 columns)
                initialFirstVisibleItemIndex = 54
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(60) { index ->
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
    fun testDefaultLazyGridStrategy_atEnd_returnsTrue() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold())
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState(
                // Start from the last items
                initialFirstVisibleItemIndex = 58
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(60) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when the last items are visible
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyGridStrategy_smallGrid_allVisible() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold(remainingItems = 6))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.height(500.dp)
            ) {
                // Small grid where all items are visible
                items(10) { index ->
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
    fun testDefaultLazyGridStrategy_customThreshold_zeroItems() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(
            LazyGridThreshold(remainingItems = 0)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState(
                initialFirstVisibleItemIndex = 59
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(60) { index ->
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
    fun testDefaultLazyGridStrategy_highRatio() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(
            LazyGridThreshold(remainingItems = 10)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(20) { index ->
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
    fun testDefaultLazyGridStrategy_largeThreshold_returnsFalse() = runComposeUiTest {
        // Large threshold that won't be met in initial state
        val strategy = defaultLazyGridStrategy(
            LazyGridThreshold(remainingItems = 100)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(200) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns false when threshold is not met
        assertTrue { result == false }
    }

    @Test
    fun testDefaultLazyGridStrategy_smallThreshold_returnsTrue() = runComposeUiTest {
        // Small threshold with position near end
        val strategy = defaultLazyGridStrategy(
            LazyGridThreshold(remainingItems = 5)
        )
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState(
                initialFirstVisibleItemIndex = 195
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(200) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when remaining items <= threshold
        assertTrue { result == true }
    }

    @Test
    fun testDefaultLazyGridStrategy_multiColumn_returnsCorrectResult() = runComposeUiTest {
        val strategy = defaultLazyGridStrategy(LazyGridThreshold(remainingItems = 8))
        var result: Boolean? = null
        setContent {
            val state = rememberLazyGridState(
                // Near the end with 3 columns
                initialFirstVisibleItemIndex = 87
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                items(90) { index ->
                    Box(modifier = Modifier.height(50.dp))
                }
            }
            result = strategy.shouldLoadMore(state)
        }

        waitForIdle()
        // Returns true when remaining items are close to threshold
        assertTrue { result == true }
    }
}
