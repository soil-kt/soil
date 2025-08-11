// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import soil.testing.UnitTest
import kotlin.test.Ignore
import kotlin.test.Test

// **NOTE:** Excluding flaky tests on CI until this commit is reflected in CMP:
// https://android-review.googlesource.com/c/platform/frameworks/support/+/3509670

@OptIn(ExperimentalTestApi::class)
class SuspenseTest : UnitTest() {

    @Ignore
    @Test
    fun testSuspense() = runUiTest {
        setContent {
            var state by remember { mutableStateOf<Loadable<String>>(Loadable.Pending) }
            Suspense(
                fallback = { Text("Loading...", modifier = Modifier.testTag("fallback")) }
            ) {
                Await(state) {
                    Text(it, modifier = Modifier.testTag("content"))
                }
            }
            Button(
                onClick = {
                    state = Loadable.Fulfilled("Hello, Soil!")
                },
                modifier = Modifier.testTag("load")
            ) {
                Text("Load")
            }
        }

        waitUntilExactlyOneExists(hasTestTag("fallback"))
        onNodeWithTag("fallback").assertTextEquals("Loading...")

        onNodeWithTag("load").performClick()
        waitUntilExactlyOneExists(hasTestTag("content"))
        onNodeWithTag("content").assertTextEquals("Hello, Soil!")
        onNodeWithTag("fallback").assertDoesNotExist()
    }
}
