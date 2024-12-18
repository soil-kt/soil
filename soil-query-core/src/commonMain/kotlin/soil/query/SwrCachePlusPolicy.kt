// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.BatchSchedulerFactory
import soil.query.core.ErrorRelay
import soil.query.core.MemoryPressure
import soil.query.core.NetworkConnectivity
import soil.query.core.WindowVisibility
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Policy for the [SwrCachePlus].
 */
@ExperimentalSoilQueryApi
interface SwrCachePlusPolicy : SwrCachePolicy {

    /**
     * Default [SubscriptionOptions] applied to [Subscription].
     */
    val subscriptionOptions: SubscriptionOptions

    /**
     * Extension receiver for referencing external instances needed when executing [subscribe][SubscriptionKey.subscribe].
     */
    val subscriptionReceiver: SubscriptionReceiver

    /**
     * Management of cached data for inactive [Subscription] instances.
     */
    val subscriptionCache: SubscriptionCache

    /**
     * The specified filter to resume subscriptions after network connectivity is reconnected.
     *
     * **Note:**
     * This setting is only effective when [networkConnectivity] is available.
     */
    val networkResumeSubscriptionsFilter: ResumeSubscriptionsFilter

    /**
     * The specified filter to resume subscriptions after window visibility is refocused.
     *
     * **Note:**
     * This setting is only effective when [windowVisibility] is available.
     */
    val windowResumeSubscriptionsFilter: ResumeSubscriptionsFilter
}

/**
 * Creates a new instance of [SwrCachePlusPolicy].
 *
 * @param coroutineScope [CoroutineScope] for coroutines executed on the [SwrCachePlus].
 * @param mainDispatcher [CoroutineDispatcher] for the main thread.
 * @param mutationOptions Default [MutationOptions] applied to [Mutation].
 * @param mutationReceiver Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
 * @param queryOptions Default [QueryOptions] applied to [Query].
 * @param queryReceiver Extension receiver for referencing external instances needed when executing [fetch][QueryKey.fetch].
 * @param queryCache Management of cached data for inactive [Query] instances.
 * @param subscriptionOptions Default [SubscriptionOptions] applied to [Subscription].
 * @param subscriptionReceiver Extension receiver for referencing external instances needed when executing [subscribe][SubscriptionKey.subscribe].
 * @param subscriptionCache Management of cached data for inactive [Subscription] instances.
 * @param batchSchedulerFactory Factory for creating a [soil.query.core.BatchScheduler].
 * @param errorRelay Relay for error handling.
 * @param memoryPressure Management of memory pressure.
 * @param networkConnectivity Management of network connectivity.
 * @param networkResumeAfterDelay Duration after which the network resumes.
 * @param networkResumeQueriesFilter Filter for resuming queries after a network error.
 * @param networkResumeSubscriptionsFilter Filter for resuming subscriptions after a network error.
 * @param windowVisibility Management of window visibility.
 * @param windowResumeQueriesFilter Filter for resuming queries after a window focus.
 * @param windowResumeSubscriptionsFilter Filter for resuming subscriptions after a window focus.
 */
@ExperimentalSoilQueryApi
fun SwrCachePlusPolicy(
    coroutineScope: CoroutineScope,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    mutationOptions: MutationOptions = MutationOptions,
    mutationReceiver: MutationReceiver = MutationReceiver,
    queryOptions: QueryOptions = QueryOptions,
    queryReceiver: QueryReceiver = QueryReceiver,
    queryCache: QueryCache = QueryCache(),
    subscriptionOptions: SubscriptionOptions = SubscriptionOptions,
    subscriptionReceiver: SubscriptionReceiver = SubscriptionReceiver,
    subscriptionCache: SubscriptionCache = SubscriptionCache(),
    batchSchedulerFactory: BatchSchedulerFactory = BatchSchedulerFactory.default(mainDispatcher),
    errorRelay: ErrorRelay? = null,
    memoryPressure: MemoryPressure = MemoryPressure,
    networkConnectivity: NetworkConnectivity = NetworkConnectivity,
    networkResumeAfterDelay: Duration = 2.seconds,
    networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),
    networkResumeSubscriptionsFilter: ResumeSubscriptionsFilter = ResumeSubscriptionsFilter(
        predicate = { it.isFailure }
    ),
    windowVisibility: WindowVisibility = WindowVisibility,
    windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    ),
    windowResumeSubscriptionsFilter: ResumeSubscriptionsFilter = ResumeSubscriptionsFilter(
        predicate = { it.isFailure }
    )
): SwrCachePlusPolicy = SwrCachePlusPolicyImpl(
    coroutineScope = coroutineScope,
    mainDispatcher = mainDispatcher,
    mutationOptions = mutationOptions,
    mutationReceiver = mutationReceiver,
    queryOptions = queryOptions,
    queryReceiver = queryReceiver,
    queryCache = queryCache,
    subscriptionOptions = subscriptionOptions,
    subscriptionReceiver = subscriptionReceiver,
    subscriptionCache = subscriptionCache,
    batchSchedulerFactory = batchSchedulerFactory,
    errorRelay = errorRelay,
    memoryPressure = memoryPressure,
    networkConnectivity = networkConnectivity,
    networkResumeAfterDelay = networkResumeAfterDelay,
    networkResumeQueriesFilter = networkResumeQueriesFilter,
    networkResumeSubscriptionsFilter = networkResumeSubscriptionsFilter,
    windowVisibility = windowVisibility,
    windowResumeQueriesFilter = windowResumeQueriesFilter,
    windowResumeSubscriptionsFilter = windowResumeSubscriptionsFilter
)

/**
 * Create a new [SwrCachePlusPolicy] instance with a receiver builder.
 *
 * @param coroutineScope [CoroutineScope] for coroutines executed on the [SwrCachePlus].
 * @param mainDispatcher [CoroutineDispatcher] for the main thread.
 * @param mutationOptions Default [MutationOptions] applied to [Mutation].
 * @param queryOptions Default [QueryOptions] applied to [Query].
 * @param queryCache Management of cached data for inactive [Query] instances.
 * @param subscriptionOptions Default [SubscriptionOptions] applied to [Subscription].
 * @param subscriptionCache Management of cached data for inactive [Subscription] instances.
 * @param batchSchedulerFactory Factory for creating a [soil.query.core.BatchScheduler].
 * @param errorRelay Relay for error handling.
 * @param memoryPressure Management of memory pressure.
 * @param networkConnectivity Management of network connectivity.
 * @param networkResumeAfterDelay Duration after which the network resumes.
 * @param networkResumeQueriesFilter Filter for resuming queries after a network error.
 * @param networkResumeSubscriptionsFilter Filter for resuming subscriptions after a network error.
 * @param windowVisibility Management of window visibility.
 * @param windowResumeQueriesFilter Filter for resuming queries after a window focus.
 * @param windowResumeSubscriptionsFilter Filter for resuming subscriptions after a window focus.
 * @param receiverBuilder Receiver builder for [MutationReceiver], [QueryReceiver] and [SubscriptionReceiver].
 */
@ExperimentalSoilQueryApi
fun SwrCachePlusPolicy(
    coroutineScope: CoroutineScope,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    mutationOptions: MutationOptions = MutationOptions,
    queryOptions: QueryOptions = QueryOptions,
    queryCache: QueryCache = QueryCache(),
    subscriptionOptions: SubscriptionOptions = SubscriptionOptions,
    subscriptionCache: SubscriptionCache = SubscriptionCache(),
    batchSchedulerFactory: BatchSchedulerFactory = BatchSchedulerFactory.default(mainDispatcher),
    errorRelay: ErrorRelay? = null,
    memoryPressure: MemoryPressure = MemoryPressure,
    networkConnectivity: NetworkConnectivity = NetworkConnectivity,
    networkResumeAfterDelay: Duration = 2.seconds,
    networkResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isFailure }
    ),
    networkResumeSubscriptionsFilter: ResumeSubscriptionsFilter = ResumeSubscriptionsFilter(
        predicate = { it.isFailure }
    ),
    windowVisibility: WindowVisibility = WindowVisibility,
    windowResumeQueriesFilter: ResumeQueriesFilter = ResumeQueriesFilter(
        predicate = { it.isStaled() }
    ),
    windowResumeSubscriptionsFilter: ResumeSubscriptionsFilter = ResumeSubscriptionsFilter(
        predicate = { it.isFailure }
    ),
    receiverBuilder: SwrReceiverBuilderPlus.() -> Unit
): SwrCachePlusPolicy {
    val commonReceiver = SwrReceiverBuilderPlusImpl().apply(receiverBuilder).build()
    return SwrCachePlusPolicyImpl(
        coroutineScope = coroutineScope,
        mainDispatcher = mainDispatcher,
        mutationOptions = mutationOptions,
        mutationReceiver = commonReceiver,
        queryOptions = queryOptions,
        queryReceiver = commonReceiver,
        queryCache = queryCache,
        subscriptionOptions = subscriptionOptions,
        subscriptionReceiver = commonReceiver,
        subscriptionCache = subscriptionCache,
        batchSchedulerFactory = batchSchedulerFactory,
        errorRelay = errorRelay,
        memoryPressure = memoryPressure,
        networkConnectivity = networkConnectivity,
        networkResumeAfterDelay = networkResumeAfterDelay,
        networkResumeQueriesFilter = networkResumeQueriesFilter,
        networkResumeSubscriptionsFilter = networkResumeSubscriptionsFilter,
        windowVisibility = windowVisibility,
        windowResumeQueriesFilter = windowResumeQueriesFilter,
        windowResumeSubscriptionsFilter = windowResumeSubscriptionsFilter
    )
}

@ExperimentalSoilQueryApi
internal class SwrCachePlusPolicyImpl(
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
    override val mutationOptions: MutationOptions,
    override val mutationReceiver: MutationReceiver,
    override val queryOptions: QueryOptions,
    override val queryReceiver: QueryReceiver,
    override val queryCache: QueryCache,
    override val subscriptionOptions: SubscriptionOptions,
    override val subscriptionReceiver: SubscriptionReceiver,
    override val subscriptionCache: SubscriptionCache,
    override val batchSchedulerFactory: BatchSchedulerFactory,
    override val errorRelay: ErrorRelay?,
    override val memoryPressure: MemoryPressure,
    override val networkConnectivity: NetworkConnectivity,
    override val networkResumeAfterDelay: Duration,
    override val networkResumeQueriesFilter: ResumeQueriesFilter,
    override val networkResumeSubscriptionsFilter: ResumeSubscriptionsFilter,
    override val windowVisibility: WindowVisibility,
    override val windowResumeQueriesFilter: ResumeQueriesFilter,
    override val windowResumeSubscriptionsFilter: ResumeSubscriptionsFilter
) : SwrCachePlusPolicy
