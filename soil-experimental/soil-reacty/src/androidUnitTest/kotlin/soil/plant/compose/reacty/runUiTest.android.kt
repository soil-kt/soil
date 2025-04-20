// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
actual fun runUiTest(
    block: ComposeUiTest.(CoroutineScope) -> Unit
) = runTest {
    runComposeUiTest(effectContext = UnconfinedTestDispatcher(testScheduler)) {
        block(backgroundScope)
    }
}
