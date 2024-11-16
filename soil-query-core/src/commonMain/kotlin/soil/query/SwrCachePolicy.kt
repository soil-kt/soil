// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import soil.query.core.BatchSchedulerFactory
import soil.query.core.ErrorRelay
import soil.query.core.MemoryPressure
import soil.query.core.NetworkConnectivity
import soil.query.core.WindowVisibility
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface SwrCachePolicy {

    /**
     * [CoroutineScope] for coroutines executed on the [SwrCache].
     *
     * **Note:**
     * The [SwrCache] internals are not thread-safe.
     * Always use a scoped implementation such as [SwrCacheScope] or [kotlinx.coroutines.MainScope] with limited concurrency.
     */
    val coroutineScope: CoroutineScope

    /**
     * [CoroutineDispatcher] for the main thread.
     *
     * **Note:**
     * Some processes are safely synchronized with the caller using the main thread.
     * When unit testing, please replace it with a test Dispatcher.
     */
    val mainDispatcher: CoroutineDispatcher

    /**
     * Default [MutationOptions] applied to [Mutation].
     */
    val mutationOptions: MutationOptions

    /**
     * Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
     */
    val mutationReceiver: MutationReceiver

    /**
     * Default [QueryOptions] applied to [Query].
     */
    val queryOptions: QueryOptions

    /**
     * Extension receiver for referencing external instances needed when executing [fetch][QueryKey.fetch].
     */
    val queryReceiver: QueryReceiver

    /**
     * Management of cached data for inactive [Query] instances.
     */
    val queryCache: QueryCache

    /**
     * Factory for creating a [soil.query.core.BatchScheduler].
     *
     * **Note:**
     * This is used for internal processes such as moving inactive query caches.
     * Please avoid changing this unless you need to substitute it for testing purposes.
     */
    val batchSchedulerFactory: BatchSchedulerFactory

    /**
     * Specify the mechanism of [ErrorRelay] when using [SwrClient.errorRelay].
     */
    val errorRelay: ErrorRelay?

    /**
     * Receiving events of memory pressure.
     */
    val memoryPressure: MemoryPressure

    /**
     * Receiving events of network connectivity.
     */
    val networkConnectivity: NetworkConnectivity

    /**
     * The delay time to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeAfterDelay: Duration

    /**
     * The specified filter to resume queries after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeQueriesFilter: ResumeQueriesFilter

    /**
     * Receiving events of window visibility.
     */
    val windowVisibility: WindowVisibility

    /**
     * The specified filter to resume queries after window visibility is refocused.
     *
     * **Note:**
     * This setting is only effective when [windowVisibility] is available.
     */
    val windowResumeQueriesFilter: ResumeQueriesFilter
}

/**
 * Create a new [SwrCachePolicy] instance.
 *
 * @param coroutineScope [CoroutineScope] for coroutines executed on the [SwrCache].
 * @param mainDispatcher [CoroutineDispatcher] for the main thread.
 * @param mutationOptions Default [MutationOptions] applied to [Mutation].
 * @param mutationReceiver Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
 * @param queryOptions Default [QueryOptions] applied to [Query].
 * @param queryReceiver Extension receiver for referencing external instances needed when executing [fetch][QueryKey.fetch].
 * @param queryCache Management of cached data for inactive [Query] instances.
 * @param batchSchedulerFactory Factory for creating a [soil.query.core.BatchScheduler].
 * @param errorRelay Specify the mechanism of [ErrorRelay] when using [SwrClient.errorRelay].
 * @param memoryPressure Receiving events of memory pressure.
 * @param networkConnectivity Receiving events of network connectivity.
 * @param networkResumeAfterDelay The delay time to resume queries after network connectivity is reconnected.
 * @param networkResumeQueriesFilter The specified filter to resume queries after network connectivity is reconnected.
 * @param windowVisibility Receiving events of window visibility.
 * @param windowResumeQueriesFilter The specified filter to resume queries after window visibility is refocused.
 */
fun SwrCachePolicy(
    coroutineScope: CoroutineScope,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    mutationOptions: MutationOptions = MutationOptions,
    mutationReceiver: MutationReceiver = MutationReceiver,
    queryOptions: QueryOptions = QueryOptions,
    queryReceiver: QueryReceiver = QueryReceiver,
    queryCache: QueryCache = QueryCache(),
    batchSchedulerFactory: BatchSchedulerFactory = BatchSchedulerFactory.default(mainDispatcher),
    errorRelay: ErrorRelay? = null,
    memoryPressure: MemoryPressure = MemoryPressure,
    networkConnectivity: NetworkConnectivity = NetworkConnectivity,
    networkResumeAfterDelay: Duration = 2.seconds,
    networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),
    windowVisibility: WindowVisibility = WindowVisibility,
    windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    )
): SwrCachePolicy = SwrCachePolicyImpl(
    coroutineScope = coroutineScope,
    mainDispatcher = mainDispatcher,
    mutationOptions = mutationOptions,
    mutationReceiver = mutationReceiver,
    queryOptions = queryOptions,
    queryReceiver = queryReceiver,
    queryCache = queryCache,
    batchSchedulerFactory = batchSchedulerFactory,
    errorRelay = errorRelay,
    memoryPressure = memoryPressure,
    networkConnectivity = networkConnectivity,
    networkResumeAfterDelay = networkResumeAfterDelay,
    networkResumeQueriesFilter = networkResumeQueriesFilter,
    windowVisibility = windowVisibility,
    windowResumeQueriesFilter = windowResumeQueriesFilter
)

/**
 * Create a new [SwrCachePolicy] instance with a receiver builder.
 *
 * @param coroutineScope [CoroutineScope] for coroutines executed on the [SwrCache].
 * @param mainDispatcher [CoroutineDispatcher] for the main thread.
 * @param mutationOptions Default [MutationOptions] applied to [Mutation].
 * @param queryOptions Default [QueryOptions] applied to [Query].
 * @param queryCache Management of cached data for inactive [Query] instances.
 * @param batchSchedulerFactory Factory for creating a [soil.query.core.BatchScheduler].
 * @param errorRelay Specify the mechanism of [ErrorRelay] when using [SwrClient.errorRelay].
 * @param memoryPressure Receiving events of memory pressure.
 * @param networkConnectivity Receiving events of network connectivity.
 * @param networkResumeAfterDelay The delay time to resume queries after network connectivity is reconnected.
 * @param networkResumeQueriesFilter The specified filter to resume queries after network connectivity is reconnected.
 * @param windowVisibility Receiving events of window visibility.
 * @param windowResumeQueriesFilter The specified filter to resume queries after window visibility is refocused.
 * @param receiverBuilder Receiver builder for [MutationReceiver] and [QueryReceiver].
 */
fun SwrCachePolicy(
    coroutineScope: CoroutineScope,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    mutationOptions: MutationOptions = MutationOptions,
    queryOptions: QueryOptions = QueryOptions,
    queryCache: QueryCache = QueryCache(),
    batchSchedulerFactory: BatchSchedulerFactory = BatchSchedulerFactory.default(mainDispatcher),
    errorRelay: ErrorRelay? = null,
    memoryPressure: MemoryPressure = MemoryPressure,
    networkConnectivity: NetworkConnectivity = NetworkConnectivity,
    networkResumeAfterDelay: Duration = 2.seconds,
    networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),
    windowVisibility: WindowVisibility = WindowVisibility,
    windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    ),
    receiverBuilder: SwrReceiverBuilder.() -> Unit
): SwrCachePolicy {
    val commonReceiver = SwrReceiverBuilderImpl().apply(receiverBuilder).build()
    return SwrCachePolicyImpl(
        coroutineScope = coroutineScope,
        mainDispatcher = mainDispatcher,
        mutationOptions = mutationOptions,
        mutationReceiver = commonReceiver,
        queryOptions = queryOptions,
        queryReceiver = commonReceiver,
        queryCache = queryCache,
        batchSchedulerFactory = batchSchedulerFactory,
        errorRelay = errorRelay,
        memoryPressure = memoryPressure,
        networkConnectivity = networkConnectivity,
        networkResumeAfterDelay = networkResumeAfterDelay,
        networkResumeQueriesFilter = networkResumeQueriesFilter,
        windowVisibility = windowVisibility,
        windowResumeQueriesFilter = windowResumeQueriesFilter
    )
}

internal class SwrCachePolicyImpl(
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
    override val mutationOptions: MutationOptions,
    override val mutationReceiver: MutationReceiver,
    override val queryOptions: QueryOptions,
    override val queryReceiver: QueryReceiver,
    override val queryCache: QueryCache,
    override val batchSchedulerFactory: BatchSchedulerFactory,
    override val errorRelay: ErrorRelay?,
    override val memoryPressure: MemoryPressure,
    override val networkConnectivity: NetworkConnectivity,
    override val networkResumeAfterDelay: Duration,
    override val networkResumeQueriesFilter: ResumeQueriesFilter,
    override val windowVisibility: WindowVisibility,
    override val windowResumeQueriesFilter: ResumeQueriesFilter
) : SwrCachePolicy
