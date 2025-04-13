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
import soil.query.annotation.InternalSoilQueryApi
import soil.query.core.BatchScheduler
import soil.query.core.BatchSchedulerFactory
import soil.query.core.Effect
import soil.query.core.EffectContext
import soil.query.core.ErrorRecord
import soil.query.core.ErrorRelay
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel
import soil.query.core.NetworkConnectivity
import soil.query.core.TimeBasedCache
import soil.query.core.UniqueId
import soil.query.core.WindowVisibility
import soil.query.core.observeOnMemoryPressure
import soil.query.core.observeOnNetworkReconnect
import soil.query.core.observeOnWindowFocus
import kotlin.time.Duration

/**
 * Implementation of the [SwrClient] interface.
 *
 * [Query] internally manages two categories:
 * - Active: When there is one or more references or within the [QueryOptions.keepAliveTime] period
 * - Inactive: When there are no references and past the [QueryOptions.keepAliveTime] period
 *
 * [Query] in the Active state does not disappear from memory unless one of the following conditions is met:
 * - [removeQueries] is explicitly called
 *
 * On the other hand, [Query] in the Inactive state gradually disappears from memory when one of the following conditions is met:
 * - Exceeds the maximum retention count of [TimeBasedCache]
 * - Past the [QueryOptions.gcTime] period since saved in [TimeBasedCache]
 * - [gc] is executed for unnecessary memory release
 *
 * [Mutation] is managed similarly to Active state [Query], but it is not explicitly deleted like [removeQueries].
 * Typically, since the result of [Mutation] execution is not reused, it does not cache after going inactive.
 *
 * @constructor Creates a new [SwrCache] instance.
 */
@OptIn(InternalSoilQueryApi::class)
class SwrCache internal constructor(
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
    override val mutationOptions: MutationOptions,
    override val mutationReceiver: MutationReceiver,
    override val mutationStore: MutableMap<UniqueId, ManagedMutation<*>>,
    override val queryOptions: QueryOptions,
    override val queryReceiver: QueryReceiver,
    override val queryStore: MutableMap<UniqueId, ManagedQuery<*>>,
    override val queryCache: QueryCache,
    override val errorRelaySource: ErrorRelay?,
    private val memoryPressure: MemoryPressure,
    private val networkConnectivity: NetworkConnectivity,
    private val networkResumeAfterDelay: Duration,
    private val networkResumeQueriesFilter: ResumeQueriesFilter,
    private val windowVisibility: WindowVisibility,
    private val windowResumeQueriesFilter: ResumeQueriesFilter,
    batchSchedulerFactory: BatchSchedulerFactory
) : SwrCacheInternal(), SwrCacheView, SwrClient {

    @Suppress("unused")
    constructor(coroutineScope: CoroutineScope) : this(SwrCachePolicy(coroutineScope))

    constructor(policy: SwrCachePolicy) : this(
        coroutineScope = policy.coroutineScope,
        mainDispatcher = policy.mainDispatcher,
        mutationOptions = policy.mutationOptions,
        mutationReceiver = policy.mutationReceiver,
        mutationStore = mutableMapOf(),
        queryOptions = policy.queryOptions,
        queryReceiver = policy.queryReceiver,
        queryStore = mutableMapOf(),
        queryCache = policy.queryCache,
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
    override val effectContext: EffectContext = EffectContext(
        queryEffectClientPropertyKey to this
    )

    private var mountedIds: Set<String> = emptySet()
    private var mountedScope: CoroutineScope? = null

    // ----- SwrCacheView ----- //

    override val mutationStoreView: Map<UniqueId, Mutation<*>>
        get() = mutationStore

    override val queryStoreView: Map<UniqueId, Query<*>>
        get() = queryStore

    // ----- SwrClient ----- //

    override val errorRelay: Flow<ErrorRecord>
        get() = errorRelaySource?.receiveAsFlow() ?: error("policy.errorRelay is not configured :(")

    override fun gc(level: MemoryPressureLevel) {
        when (level) {
            MemoryPressureLevel.Low -> queryCache.evict()
            MemoryPressureLevel.High -> queryCache.clear()
        }
    }

    override fun purgeAll() {
        resetQueries()
        resetMutations()
    }

    override fun effect(block: Effect): Job {
        return launch(block)
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
            resumeQueriesWhenNetworkReconnect(networkResumeQueriesFilter)
        }
    }

    private suspend fun observeWindowVisibility() {
        if (windowVisibility == WindowVisibility.Unsupported) return
        observeOnWindowFocus(windowVisibility) {
            resumeQueriesWhenWindowFocus(windowResumeQueriesFilter)
        }
    }
}
