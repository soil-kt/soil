// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.annotation.InternalSoilQueryApi
import soil.query.core.BatchScheduler
import soil.query.core.BatchSchedulerFactory
import soil.query.core.ErrorRecord
import soil.query.core.ErrorRelay
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel
import soil.query.core.NetworkConnectivity
import soil.query.core.UniqueId
import soil.query.core.WindowVisibility
import soil.query.core.observeOnMemoryPressure
import soil.query.core.observeOnNetworkReconnect
import soil.query.core.observeOnWindowFocus
import kotlin.time.Duration

/**
 * An enhanced version of [SwrCache] that integrates [SwrClientPlus] into SwrCache.
 */
@ExperimentalSoilQueryApi
@OptIn(InternalSoilQueryApi::class)
class SwrCachePlus internal constructor(
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
    override val mutationOptions: MutationOptions,
    override val mutationReceiver: MutationReceiver,
    override val mutationStore: MutableMap<UniqueId, ManagedMutation<*>>,
    override val queryOptions: QueryOptions,
    override val queryReceiver: QueryReceiver,
    override val queryStore: MutableMap<UniqueId, ManagedQuery<*>>,
    override val queryCache: QueryCache,
    override val subscriptionOptions: SubscriptionOptions,
    override val subscriptionReceiver: SubscriptionReceiver,
    override val subscriptionStore: MutableMap<UniqueId, ManagedSubscription<*>>,
    override val subscriptionCache: SubscriptionCache,
    override val errorRelaySource: ErrorRelay?,
    private val memoryPressure: MemoryPressure,
    private val networkConnectivity: NetworkConnectivity,
    private val networkResumeAfterDelay: Duration,
    private val networkResumeQueriesFilter: ResumeQueriesFilter,
    private val windowVisibility: WindowVisibility,
    private val windowResumeQueriesFilter: ResumeQueriesFilter,
    batchSchedulerFactory: BatchSchedulerFactory
) : SwrCachePlusInternal(), SwrCachePlusView, SwrClientPlus {

    @Suppress("unused")
    constructor(coroutineScope: CoroutineScope) : this(SwrCachePlusPolicy(coroutineScope))

    constructor(policy: SwrCachePlusPolicy) : this(
        coroutineScope = policy.coroutineScope,
        mainDispatcher = policy.mainDispatcher,
        mutationOptions = policy.mutationOptions,
        mutationReceiver = policy.mutationReceiver,
        mutationStore = mutableMapOf(),
        queryOptions = policy.queryOptions,
        queryReceiver = policy.queryReceiver,
        queryStore = mutableMapOf(),
        queryCache = policy.queryCache,
        subscriptionOptions = policy.subscriptionOptions,
        subscriptionReceiver = policy.subscriptionReceiver,
        subscriptionStore = mutableMapOf(),
        subscriptionCache = policy.subscriptionCache,
        errorRelaySource = policy.errorRelay,
        memoryPressure = policy.memoryPressure,
        networkConnectivity = policy.networkConnectivity,
        networkResumeAfterDelay = policy.networkResumeAfterDelay,
        networkResumeQueriesFilter = policy.networkResumeQueriesFilter,
        windowVisibility = policy.windowVisibility,
        windowResumeQueriesFilter = policy.windowResumeQueriesFilter,
        batchSchedulerFactory = policy.batchSchedulerFactory
    )

    override val batchScheduler: BatchScheduler = batchSchedulerFactory.create(coroutineScope)

    private var mountedIds: Set<String> = emptySet()
    private var mountedScope: CoroutineScope? = null

    // ----- SwrCacheView ----- //

    override val mutationStoreView: Map<UniqueId, Mutation<*>>
        get() = mutationStore

    override val queryStoreView: Map<UniqueId, Query<*>>
        get() = queryStore

    override val subscriptionStoreView: Map<UniqueId, Subscription<*>>
        get() = subscriptionStore

    // ----- SwrClientPlus ----- //

    override val errorRelay: Flow<ErrorRecord>
        get() = errorRelaySource?.receiveAsFlow() ?: error("policy.errorRelay is not configured :(")

    override fun gc(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.Low -> {
                queryCache.evict()
                subscriptionCache.evict()
            }

            MemoryPressureLevel.High -> {
                queryCache.clear()
                subscriptionCache.clear()
            }
        }
    }

    override fun purgeAll() {
        resetQueries()
        resetMutations()
        resetSubscriptions()
    }

    override fun perform(sideEffects: QueryEffect): Job {
        return launch(sideEffects)
    }

    override fun onMount(id: String) {
        if (mountedIds.isEmpty()) {
            val scope = CoroutineScope(context = newCoroutineContext(coroutineScope))
            scope.launch { observeMemoryPressure() }
            scope.launch { observeNetworkConnectivity() }
            scope.launch { observeWindowVisibility() }
            mountedScope = scope
        }
        mountedIds += id
    }

    override fun onUnmount(id: String) {
        mountedIds -= id
        if (mountedIds.isEmpty()) {
            mountedScope?.cancel()
            mountedScope = null
        }
    }

    private suspend fun observeMemoryPressure() {
        if (memoryPressure == MemoryPressure.Unsupported) return
        observeOnMemoryPressure(memoryPressure) { level ->
            withContext(mainDispatcher) {
                gc(level)
            }
        }
    }

    private suspend fun observeNetworkConnectivity() {
        if (networkConnectivity == NetworkConnectivity.Unsupported) return
        observeOnNetworkReconnect(networkConnectivity, networkResumeAfterDelay) {
            perform {
                forEach(networkResumeQueriesFilter) { id, _ ->
                    queryStore[id]
                        ?.takeIf { it.options.revalidateOnReconnect }
                        ?.resume()
                }
            }
        }
    }

    private suspend fun observeWindowVisibility() {
        if (windowVisibility == WindowVisibility.Unsupported) return
        observeOnWindowFocus(windowVisibility) {
            perform {
                forEach(windowResumeQueriesFilter) { id, _ ->
                    queryStore[id]
                        ?.takeIf { it.options.revalidateOnFocus }
                        ?.resume()
                }
            }
        }
    }
}
