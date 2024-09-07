// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * [CoroutineScope] with limited concurrency for [SwrCache] and [SwrCachePlus].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwrCacheScope(parent: Job? = null) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        SupervisorJob(parent) +
            Dispatchers.Default.limitedParallelism(1) +
                CoroutineName("SwrCache")
}
