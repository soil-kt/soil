// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CoroutineScope
import soil.query.SwrCacheScope

@OptIn(ExperimentalTestApi::class)
actual fun runUiTest(
    block: ComposeUiTest.(CoroutineScope) -> Unit
) = runComposeUiTest {
    block(SwrCacheScope())
}
