// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file: Suppress("unused")

package soil.plant.compose.reacty

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * NOTE: Uses Coroutine Test to run UI tests because Coroutine behavior differs across platforms.
 * TestScope requires calling runCurrent on the test side to advance Channel(Command) processing inside SwrCache.
 */
@OptIn(ExperimentalTestApi::class)
fun runUiTest(
    block: ComposeUiTest.(TestScope) -> Unit
): TestResult = runTest {
    runComposeUiTest {
        block(this@runTest)
    }
}
