// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.annotation.InternalSoilQueryApi
import soil.query.core.Actor
import soil.query.core.ActorBlockRunner
import soil.query.core.FilterType
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.UniqueId
import soil.query.core.WhileSubscribedAlt
import soil.query.core.epoch
import soil.query.core.forEach
import soil.query.core.getOrNull
import soil.query.core.retryWithExponentialBackoff
import soil.query.core.toResultFlow
import soil.query.core.vvv

@ExperimentalSoilQueryApi
@InternalSoilQueryApi
abstract class SwrCachePlusInternal : SwrCacheInternal(), SubscriptionClient, SubscriptionEffectClient {

    protected abstract val subscriptionOptions: SubscriptionOptions
    protected abstract val subscriptionReceiver: SubscriptionReceiver
    protected abstract val subscriptionStore: MutableMap<UniqueId, ManagedSubscription<*>>
    protected abstract val subscriptionCache: SubscriptionCache

    protected fun resetSubscriptions() {
        val subscriptionStoreCopy = subscriptionStore.toMap()
        subscriptionStore.clear()
        subscriptionCache.clear()
        subscriptionStoreCopy.values.forEach { it.cancel() }
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
                options = key.onConfigureOptions()?.invoke(subscriptionOptions) ?: subscriptionOptions,
                initialValue = subscriptionCache[key.id] as? SubscriptionState<T> ?: newSubscriptionState(key),
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
        val event = MutableSharedFlow<SubscriptionEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val state = MutableStateFlow(initialValue)
        val reducer = createSubscriptionReducer<T>()
        val dispatch: SubscriptionDispatch<T> = { action ->
            options.vvv(id) { "dispatching $action" }
            state.value = reducer(state.value, action)
        }
        val refresh = MutableStateFlow(0L)
        val restart: SubscriptionRestart = { restartAt ->
            scope.launch { refresh.emit(restartAt) }
        }
        val relay: SubscriptionErrorRelay? = errorRelaySource?.let { it::send }
        val command = Channel<SubscriptionCommand<T>>()
        val actor = ActorBlockRunner(
            scope = scope,
            options = options,
            onTimeout = {
                scope.launch { batchScheduler.post { deactivateSubscription<T>(id) } }
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
            event = event,
            state = state,
            command = command,
            actor = actor,
            dispatch = dispatch,
            cacheable = contentCacheable
        )
    }

    private fun <T> newSubscriptionState(key: SubscriptionKey<T>): SubscriptionState<T> {
        val onInitialData = key.onInitialData() ?: return SubscriptionState()
        val initialData = with(this) { onInitialData() } ?: return SubscriptionState()
        return SubscriptionState.success(data = initialData, dataUpdatedAt = 0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> deactivateSubscription(id: UniqueId) {
        val subscription = subscriptionStore[id] as? ManagedSubscription<T> ?: return
        if (subscription.hasAttachedInstances()) {
            subscription.options.vvv(subscription.id) { "deactivate aborted: instances attached" }
            return
        }
        subscriptionStore.remove(id)
        subscription.cancel()
        subscription.options.vvv(subscription.id) { "deactivated" }
        saveToCache(subscription)
    }

    private fun <T> saveToCache(subscription: ManagedSubscription<T>) {
        val lastValue = subscription.state.value
        val ttl = subscription.options.gcTime
        val saveable = lastValue.isSuccess && ttl.isPositive()
        if (saveable && subscription.isCacheable(lastValue.reply)) {
            subscriptionCache.set(subscription.id, lastValue, ttl)
            subscription.options.vvv(subscription.id) { "cached(ttl=$ttl)" }
        } else {
            subscriptionCache.delete(subscription.id)
        }
    }

    // ----- SubscriptionEffectClient ----- //
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscriptionData(id: SubscriptionId<T>): T? {
        val subscription = subscriptionStore[id] as? ManagedSubscription<T>
        if (subscription != null) {
            return subscription.state.value.reply.getOrNull()
        }
        val state = subscriptionCache[id] as? SubscriptionState<T>
        if (state != null) {
            return state.reply.getOrNull()
        }
        return null
    }

    override fun <T> updateSubscriptionData(
        id: SubscriptionId<T>,
        edit: T.() -> T
    ) {
        val data = getSubscriptionData(id)
        if (data != null) {
            setSubscriptionData(id, data.edit())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> setSubscriptionData(id: SubscriptionId<T>, data: T) {
        val subscription = subscriptionStore[id] as? ManagedSubscription<T>
        subscription?.forceUpdate(data)
        subscriptionCache.swap(id) {
            this as SubscriptionState<T>
            patch(data)
        }
    }

    override fun removeSubscriptions(filter: RemoveSubscriptionsFilter) {
        SubscriptionFilterResolver(subscriptionStore, subscriptionCache)
            .forEach(filter, ::removeSubscription)
    }

    override fun <U : UniqueId> removeSubscriptionsBy(vararg ids: U) {
        require(ids.isNotEmpty())
        ids.forEach { id ->
            removeSubscription(id, FilterType.Active)
            removeSubscription(id, FilterType.Inactive)
        }
    }

    private fun removeSubscription(id: UniqueId, type: FilterType) {
        when (type) {
            FilterType.Active -> subscriptionStore.remove(id)?.cancel()
            FilterType.Inactive -> subscriptionCache.delete(id)
        }
    }

    override fun resumeSubscriptions(filter: ResumeSubscriptionsFilter) {
        // NOTE: resume targets only active subscriptions.
        SubscriptionFilterResolver(subscriptionStore, subscriptionCache)
            .forEach(filter) { id, _ -> resumeSubscription(id) }
    }

    protected fun resumeSubscriptionsWhenNetworkReconnect(filter: ResumeSubscriptionsFilter) {
        SubscriptionFilterResolver(subscriptionStore, subscriptionCache)
            .forEach(filter) { id, _ ->
                resumeSubscription(id) { it.options.restartOnReconnect }
            }
    }

    protected fun resumeSubscriptionsWhenWindowFocus(filter: ResumeSubscriptionsFilter) {
        SubscriptionFilterResolver(subscriptionStore, subscriptionCache)
            .forEach(filter) { id, _ ->
                resumeSubscription(id) { it.options.restartOnFocus }
            }
    }

    override fun <U : UniqueId> resumeSubscriptionsBy(vararg ids: U) {
        require(ids.isNotEmpty())
        ids.forEach { id -> resumeSubscription(id) }
    }

    private fun resumeSubscription(id: UniqueId) {
        subscriptionStore[id]?.resume()
    }

    private fun resumeSubscription(id: UniqueId, predicate: (ManagedSubscription<*>) -> Boolean) {
        subscriptionStore[id]?.takeIf(predicate)?.resume()
    }

    @InternalSoilQueryApi
    class ManagedSubscription<T> internal constructor(
        val id: UniqueId,
        val options: SubscriptionOptions,
        override val source: SharedFlow<Result<T>>,
        override val event: MutableSharedFlow<SubscriptionEvent>,
        override val state: StateFlow<SubscriptionState<T>>,
        override val command: SendChannel<SubscriptionCommand<T>>,
        private val scope: CoroutineScope,
        private val actor: ActorBlockRunner,
        private val dispatch: SubscriptionDispatch<T>,
        private val cacheable: SubscriptionContentCacheable<T>?
    ) : Subscription<T>, Actor by actor {

        fun cancel() {
            scope.cancel()
        }

        fun resume() {
            event.tryEmit(SubscriptionEvent.Resume)
        }

        fun forceUpdate(data: T) {
            dispatch(SubscriptionAction.ForceUpdate(data = data, dataUpdatedAt = epoch()))
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
