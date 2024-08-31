// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import soil.query.core.BatchScheduler
import soil.query.core.BatchSchedulerFactory
import soil.query.core.ErrorRelay
import soil.query.core.MemoryPressure
import soil.query.core.NetworkConnectivity
import soil.query.core.WindowVisibility
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Policy for the [SwrCache].
 */
class SwrCachePolicy(

    /**
     * [CoroutineScope] for coroutines executed on the [SwrCache].
     *
     * **Note:**
     * The [SwrCache] internals are not thread-safe.
     * Always use a scoped implementation such as [SwrCacheScope] or [kotlinx.coroutines.MainScope] with limited concurrency.
     */
    val coroutineScope: CoroutineScope,

    /**
     * [CoroutineDispatcher] for the main thread.
     *
     * **Note:**
     * Some processes are safely synchronized with the caller using the main thread.
     * When unit testing, please replace it with a test Dispatcher.
     */
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,

    /**
     * Default [MutationOptions] applied to [Mutation].
     */
    val mutationOptions: MutationOptions = MutationOptions,

    /**
     * Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
     */
    val mutationReceiver: MutationReceiver = MutationReceiver,

    /**
     * Default [QueryOptions] applied to [Query].
     */
    val queryOptions: QueryOptions = QueryOptions,

    /**
     * Extension receiver for referencing external instances needed when executing [fetch][QueryKey.fetch].
     */
    val queryReceiver: QueryReceiver = QueryReceiver,

    /**
     * Management of cached data for inactive [Query] instances.
     */
    val queryCache: QueryCache = QueryCache(),

    /**
     * Scheduler for batching tasks.
     *
     * **Note:**
     * This is used for internal processes such as moving inactive query caches.
     * Please avoid changing this unless you need to substitute it for testing purposes.
     */
    val batchSchedulerFactory: BatchSchedulerFactory = BatchSchedulerFactory.default(mainDispatcher),

    /**
     * Specify the mechanism of [ErrorRelay] when using [SwrClient.errorRelay].
     */
    val errorRelay: ErrorRelay? = null,

    /**
     * Receiving events of memory pressure.
     */
    val memoryPressure: MemoryPressure = MemoryPressure,

    /**
     * Receiving events of network connectivity.
     */
    val networkConnectivity: NetworkConnectivity = NetworkConnectivity,

    /**
     * The delay time to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeAfterDelay: Duration = 2.seconds,

    /**
     * The specified filter to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),

    /**
     * Receiving events of window visibility.
     */
    val windowVisibility: WindowVisibility = WindowVisibility,

    /**
     * The specified filter to resume queries after window visibility is refocused.
     *
     * **Note:**
     * This setting is only effective when [windowVisibility] is available.
     */
    val windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    )
)
