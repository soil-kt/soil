// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file: Suppress("unused")

package soil.query.compose.runtime

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlinx.coroutines.CoroutineScope

/**
 * FIXME Workaround for flaky tests.
 *
 * NOTE:
 * Upon investigation, we found that this is a known issue reported in the Google Issue Tracker.
 *
 * - https://github.com/soil-kt/soil/pull/152
 * - https://github.com/soil-kt/soil/actions/runs/14419696209/job/40440710141
 * - https://issuetracker.google.com/issues/321690042
 *
 * To work around this issue, we're attempting to pass `effectContext` to `runComposeUiTest`.
 * Unfortunately, `effectContext` is not yet supported on non-Android platforms (Skiko),
 * even in the latest versions such as 1.8+dev2308.
 *
 * As a result, we introduced the `runUiTest` function to abstract the test runner
 * and switch between Android and Skiko implementations accordingly.
 */
@OptIn(ExperimentalTestApi::class)
expect fun runUiTest(
    block: ComposeUiTest.(CoroutineScope) -> Unit
)
