// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.ActorBlockRunner
import soil.query.core.ActorSequenceNumber
import soil.query.core.BatchScheduler
import soil.query.core.Marker
import soil.query.core.MemoryPressureLevel
import soil.query.core.Reply
import soil.query.core.UniqueId
import soil.query.core.WhileSubscribedAlt
import soil.query.core.epoch
import soil.query.core.retryWithExponentialBackoff
import soil.query.core.toResultFlow
import soil.query.core.vvv

/**
 * An enhanced version of [SwrCache] that integrates [SwrClientPlus] into SwrCache.
 */
@ExperimentalSoilQueryApi
class SwrCachePlus(private val policy: SwrCachePlusPolicy) : SwrCache(policy), SwrClientPlus {

    @Suppress("unused")
    constructor(coroutineScope: CoroutineScope) : this(SwrCachePlusPolicy(coroutineScope))

    private val subscriptionReceiver = policy.subscriptionReceiver
    private val subscriptionStore: MutableMap<UniqueId, ManagedSubscription<*>> = mutableMapOf()
    private val subscriptionCache = policy.subscriptionCache

    private val coroutineScope: CoroutineScope = CoroutineScope(
        context = newCoroutineContext(policy.coroutineScope)
    )
    private val batchScheduler: BatchScheduler = policy.batchSchedulerFactory.create(coroutineScope)


    // ----- SwrClientPlus ----- //

    override val defaultSubscriptionOptions: SubscriptionOptions = policy.subscriptionOptions

    override fun gc(level: MemoryPressureLevel) {
        super.gc(level)
        when (level) {
            MemoryPressureLevel.Low -> subscriptionCache.evict()
            MemoryPressureLevel.High -> subscriptionCache.clear()
        }
    }

    override fun purgeAll() {
        super.purgeAll()
        purgeAllSubscriptions()
    }

    private fun purgeAllSubscriptions() {
        val subscriptionStoreCopy = subscriptionStore.toMap()
        subscriptionStore.clear()
        subscriptionCache.clear()
        subscriptionStoreCopy.values.forEach { it.close() }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker
    ): SubscriptionRef<T> {
        val id = key.id
        var subscription = subscriptionStore[id] as? ManagedSubscription<T>
        if (subscription == null) {
            subscription = newSubscription(
                id = id,
                options = key.onConfigureOptions()?.invoke(defaultSubscriptionOptions) ?: defaultSubscriptionOptions,
                initialValue = subscriptionCache[key.id] as? SubscriptionState<T> ?: SubscriptionState(),
                subscribe = key.subscribe,
                contentCacheable = key.contentCacheable
            ).also { subscriptionStore[id] = it }
        }
        return SubscriptionRef(
            key = key,
            marker = marker,
            subscription = subscription
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> newSubscription(
        id: UniqueId,
        options: SubscriptionOptions,
        initialValue: SubscriptionState<T>,
        subscribe: SubscriptionReceiver.() -> Flow<T>,
        contentCacheable: SubscriptionContentCacheable<T>?
    ): ManagedSubscription<T> {
        val scope = CoroutineScope(newCoroutineContext(coroutineScope))
        val state = MutableStateFlow(initialValue)
        val reducer = createSubscriptionReducer<T>()
        val dispatch: SubscriptionDispatch<T> = { action ->
            options.vvv(id) { "dispatching $action" }
            state.value = reducer(state.value, action)
        }
        val refresh = MutableStateFlow(epoch())
        val restart: SubscriptionRestart = {
            scope.launch { refresh.emit(epoch()) }
        }
        val relay: SubscriptionErrorRelay? = policy.errorRelay?.let { it::send }
        val command = Channel<SubscriptionCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = { seq ->
                scope.launch { batchScheduler.post { closeSubscription<T>(id, seq) } }
            }
        ) {
            for (c in command) {
                options.vvv(id) { "next command $c" }
                c.handle(
                    ctx = ManagedSubscriptionContext(
                        options = options,
                        state = state.value,
                        dispatch = dispatch,
                        restart = restart,
                        relay = relay
                    )
                )
            }
        }

        val subscribeFlow = subscriptionReceiver.subscribe()
        val source = refresh
            .flatMapLatest {
                subscribeFlow
                    .retryWithExponentialBackoff(options) { err, count, nextBackOff ->
                        options.vvv(id) { "retry(count=$count next=$nextBackOff error=${err.message})" }
                    }
                    .toResultFlow()
            }
            .shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribedAlt(
                    stopTimeout = options.keepAliveTime,
                    onSubscriptionCount = { subscribers ->
                        options.vvv(id) { "subscription count: $subscribers" }
                    }
                )
            )
        return ManagedSubscription(
            scope = scope,
            id = id,
            options = options,
            source = source,
            state = state,
            command = command,
            actor = actor,
            cacheable = contentCacheable
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> closeSubscription(id: UniqueId, seq: ActorSequenceNumber) {
        val subscription = subscriptionStore[id] as? ManagedSubscription<T> ?: return
        if (subscription.actor.seq == seq) {
            subscriptionStore.remove(id)
            subscription.close()
            saveToCache(subscription)
        }
    }

    private fun <T> saveToCache(subscription: ManagedSubscription<T>) {
        val lastValue = subscription.state.value
        val ttl = subscription.options.gcTime
        val saveable = lastValue.isSuccess && ttl.isPositive()
        if (saveable && subscription.isCacheable(lastValue.reply)) {
            subscriptionCache.set(subscription.id, lastValue, ttl)
            subscription.options.vvv(subscription.id) { "cached(ttl=$ttl)" }
        }
    }

    internal class ManagedSubscription<T>(
        val scope: CoroutineScope,
        val id: UniqueId,
        val options: SubscriptionOptions,
        override val source: SharedFlow<Result<T>>,
        override val state: StateFlow<SubscriptionState<T>>,
        override val command: SendChannel<SubscriptionCommand<T>>,
        internal val actor: ActorBlockRunner,
        private val cacheable: SubscriptionContentCacheable<T>?
    ) : Subscription<T> {

        override fun launchIn(scope: CoroutineScope): Job {
            return actor.launchIn(scope)
        }

        fun close() {
            scope.cancel()
            command.close()
        }

        fun isCacheable(reply: Reply<T>): Boolean {
            return when (reply) {
                is Reply.None -> false
                is Reply.Some<T> -> {
                    cacheable?.invoke(reply.value) ?: true
                }
            }
        }
    }

    internal class ManagedSubscriptionContext<T>(
        override val options: SubscriptionOptions,
        override val state: SubscriptionState<T>,
        override val dispatch: SubscriptionDispatch<T>,
        override val restart: SubscriptionRestart,
        override val relay: SubscriptionErrorRelay?
    ) : SubscriptionCommand.Context<T>
}
