// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import soil.query.SwrCachePlusPolicy
import soil.query.SwrCachePolicy
import soil.query.annotation.ExperimentalSoilQueryApi

fun TestScope.newSwrCachePolicy() = SwrCachePolicy(
    coroutineScope = backgroundScope,
    mainDispatcher = StandardTestDispatcher(testScheduler)
)

@OptIn(ExperimentalSoilQueryApi::class)
fun TestScope.newSwrCachePlusPolicy() = SwrCachePlusPolicy(
    coroutineScope = backgroundScope,
    mainDispatcher = StandardTestDispatcher(testScheduler)
)
