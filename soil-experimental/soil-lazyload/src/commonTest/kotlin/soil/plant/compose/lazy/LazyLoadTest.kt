// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.lazy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.unit.dp
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LazyLoadTest : UnitTest() {

    @Test
    fun testLazyLoad_withLazyList() = runComposeUiTest {
        var loadMoreCalled = 0
        var pageNumber = 0
        val loadMore = LazyLoadMore<Int> { param ->
            loadMoreCalled += 1
            pageNumber = param
        }
        val items = List(30) { "Item $it" }
        setContent {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("lazyColumn"),
                state = lazyListState,
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(16.dp)
                            .testTag(item)
                    )
                }
            }
            LazyLoad(
                state = lazyListState,
                loadMore = loadMore,
                loadMoreParam = 1,
                threshold = LazyListThreshold(
                    remainingItems = 6
                )
            )
        }

        waitUntilExactlyOneExists(hasTestTag("Item 0"))
        assertEquals(0, loadMoreCalled)
        assertEquals(0, pageNumber)

        onNodeWithTag("lazyColumn")
            .performScrollToIndex(items.lastIndex - 6)

        waitUntilExactlyOneExists(hasTestTag("Item ${items.lastIndex - 6}"))
        waitUntil { loadMoreCalled == 1 }
        assertEquals(1, pageNumber)

        onNodeWithTag("lazyColumn")
            .performScrollToIndex(0)

        waitUntilExactlyOneExists(hasTestTag("Item 0"))

        onNodeWithTag("lazyColumn")
            .performScrollToIndex(items.lastIndex - 6)

        waitUntilExactlyOneExists(hasTestTag("Item ${items.lastIndex - 6}"))
        waitForIdle()
        // LazyLoad called only once
        assertEquals(1, loadMoreCalled)
        assertEquals(1, pageNumber)
    }

    @Test
    fun testLazyLoad_withLazyGrid() = runComposeUiTest {
        var loadMoreCalled = 0
        var pageNumber = 0
        val loadMore = LazyLoadMore<Int> { param ->
            loadMoreCalled += 1
            pageNumber = param
        }
        val items = List(30) { "Item $it" }
        setContent {
            val lazyGridState = rememberLazyGridState()
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize().testTag("lazyGrid"),
                state = lazyGridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(16.dp)
                            .testTag(item)
                    )
                }
            }
            LazyLoad(
                state = lazyGridState,
                loadMore = loadMore,
                loadMoreParam = 1,
                threshold = LazyGridThreshold(
                    remainingItems = 6
                )
            )
        }

        waitUntilExactlyOneExists(hasTestTag("Item 0"))
        assertEquals(0, loadMoreCalled)
        assertEquals(0, pageNumber)

        onNodeWithTag("lazyGrid")
            .performScrollToIndex(items.lastIndex - 6)

        waitUntilExactlyOneExists(hasTestTag("Item ${items.lastIndex - 6}"))
        waitUntil { loadMoreCalled == 1 }
        assertEquals(1, pageNumber)

        onNodeWithTag("lazyGrid")
            .performScrollToIndex(0)

        waitUntilExactlyOneExists(hasTestTag("Item 0"))

        onNodeWithTag("lazyGrid")
            .performScrollToIndex(items.lastIndex - 6)

        waitUntilExactlyOneExists(hasTestTag("Item ${items.lastIndex - 6}"))
        // LazyLoad called only once
        waitForIdle()
        assertEquals(1, loadMoreCalled)
        assertEquals(1, pageNumber)
    }

    @Test
    fun testLazyLoad_nextParamIsNull() = runComposeUiTest {
        var loadMoreCalled = 0
        var pageNumber = 0
        val loadMore = LazyLoadMore<Int> { param ->
            loadMoreCalled += 1
            pageNumber = param
        }
        val items = List(30) { "Item $it" }
        setContent {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("lazyColumn"),
                state = lazyListState,
                contentPadding = PaddingValues(16.dp)
            ) {
                items(items) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(16.dp)
                            .testTag(item)
                    )
                }
            }
            LazyLoad(
                state = lazyListState,
                loadMore = loadMore,
                loadMoreParam = null,
                threshold = LazyListThreshold(
                    remainingItems = 6
                )
            )
        }

        waitUntilExactlyOneExists(hasTestTag("Item 0"))
        assertEquals(0, loadMoreCalled)
        assertEquals(0, pageNumber)

        onNodeWithTag("lazyColumn")
            .performScrollToIndex(items.lastIndex - 6)

        waitUntilExactlyOneExists(hasTestTag("Item ${items.lastIndex - 6}"))
        waitForIdle()
        assertEquals(0, loadMoreCalled)
        assertEquals(0, pageNumber)
    }
}
