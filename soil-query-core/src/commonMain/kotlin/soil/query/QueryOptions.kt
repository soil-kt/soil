// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ActorOptions
import soil.query.core.LoggerFn
import soil.query.core.LoggingOptions
import soil.query.core.RetryOptions
import soil.query.core.Retryable
import soil.query.core.UniqueId
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * [QueryOptions] providing settings related to the internal behavior of an [Query].
 */
interface QueryOptions : ActorOptions, LoggingOptions, RetryOptions {

    /**
     * The duration after which the returned value of the fetch function block is considered stale.
     */
    val staleTime: Duration

    /**
     * The period during which the Key's return value, if not referenced anywhere, is temporarily cached in memory.
     */
    val gcTime: Duration

    /**
     * Maximum window time on prefetch processing.
     *
     * If this time is exceeded, the [kotlinx.coroutines.CoroutineScope] within the prefetch processing will terminate,
     * but the command processing within the Actor will continue as long as [keepAliveTime] is set.
     */
    val prefetchWindowTime: Duration

    /**
     * Determines whether query processing needs to be paused based on error.
     *
     * @see [shouldPause]
     */
    val pauseDurationAfter: ((Throwable) -> Duration?)?

    /**
     * Automatically revalidate active [Query] when the network reconnects.
     *
     * **Note:**
     * This setting is only effective when [soil.query.core.NetworkConnectivity] is available.
     */
    val revalidateOnReconnect: Boolean

    /**
     * Automatically revalidate active [Query] when the window is refocused.
     *
     * **Note:**
     * This setting is only effective when [soil.query.core.WindowVisibility] is available.
     */
    val revalidateOnFocus: Boolean

    /**
     * This callback function will be called if some query encounters an error.
     */
    val onError: ((Throwable, QueryModel<*>, UniqueId) -> Unit)?

    companion object Default : QueryOptions {
        override val staleTime: Duration = Duration.ZERO
        override val gcTime: Duration = 5.minutes
        override val prefetchWindowTime: Duration = 1.seconds
        override val pauseDurationAfter: ((Throwable) -> Duration?)? = null
        override val revalidateOnReconnect: Boolean = true
        override val revalidateOnFocus: Boolean = true
        override val onError: ((Throwable, QueryModel<*>, UniqueId) -> Unit)? = null

        // ----- ActorOptions ----- //
        override val keepAliveTime: Duration = 5.seconds

        // ----- LoggingOptions ----- //
        override val logger: LoggerFn? = null

        // ----- RetryOptions ----- //
        override val shouldRetry: (Throwable) -> Boolean = { e ->
            e is Retryable && e.canRetry
        }
        override val retryCount: Int = 3
        override val retryInitialInterval: Duration = 500.milliseconds
        override val retryMaxInterval: Duration = 30.seconds
        override val retryMultiplier: Double = 1.5
        override val retryRandomizationFactor: Double = 0.5
        override val retryRandomizer: Random = Random
    }
}

fun QueryOptions(
    staleTime: Duration = QueryOptions.staleTime,
    gcTime: Duration = QueryOptions.gcTime,
    prefetchWindowTime: Duration = QueryOptions.prefetchWindowTime,
    pauseDurationAfter: ((Throwable) -> Duration?)? = QueryOptions.pauseDurationAfter,
    revalidateOnReconnect: Boolean = QueryOptions.revalidateOnReconnect,
    revalidateOnFocus: Boolean = QueryOptions.revalidateOnFocus,
    onError: ((Throwable, QueryModel<*>, UniqueId) -> Unit)? = QueryOptions.onError,
    keepAliveTime: Duration = QueryOptions.keepAliveTime,
    logger: LoggerFn? = QueryOptions.logger,
    shouldRetry: (Throwable) -> Boolean = QueryOptions.shouldRetry,
    retryCount: Int = QueryOptions.retryCount,
    retryInitialInterval: Duration = QueryOptions.retryInitialInterval,
    retryMaxInterval: Duration = QueryOptions.retryMaxInterval,
    retryMultiplier: Double = QueryOptions.retryMultiplier,
    retryRandomizationFactor: Double = QueryOptions.retryRandomizationFactor,
    retryRandomizer: Random = QueryOptions.retryRandomizer,
): QueryOptions {
    return object : QueryOptions {
        override val staleTime: Duration = staleTime
        override val gcTime: Duration = gcTime
        override val prefetchWindowTime: Duration = prefetchWindowTime
        override val pauseDurationAfter: ((Throwable) -> Duration?)? = pauseDurationAfter
        override val revalidateOnReconnect: Boolean = revalidateOnReconnect
        override val revalidateOnFocus: Boolean = revalidateOnFocus
        override val onError: ((Throwable, QueryModel<*>, UniqueId) -> Unit)? = onError
        override val keepAliveTime: Duration = keepAliveTime
        override val logger: LoggerFn? = logger
        override val shouldRetry: (Throwable) -> Boolean = shouldRetry
        override val retryCount: Int = retryCount
        override val retryInitialInterval: Duration = retryInitialInterval
        override val retryMaxInterval: Duration = retryMaxInterval
        override val retryMultiplier: Double = retryMultiplier
        override val retryRandomizationFactor: Double = retryRandomizationFactor
        override val retryRandomizer: Random = retryRandomizer
    }
}

fun QueryOptions.copy(
    staleTime: Duration = this.staleTime,
    gcTime: Duration = this.gcTime,
    prefetchWindowTime: Duration = this.prefetchWindowTime,
    pauseDurationAfter: ((Throwable) -> Duration?)? = this.pauseDurationAfter,
    revalidateOnReconnect: Boolean = this.revalidateOnReconnect,
    revalidateOnFocus: Boolean = this.revalidateOnFocus,
    onError: ((Throwable, QueryModel<*>, UniqueId) -> Unit)? = this.onError,
    keepAliveTime: Duration = this.keepAliveTime,
    logger: LoggerFn? = this.logger,
    shouldRetry: (Throwable) -> Boolean = this.shouldRetry,
    retryCount: Int = this.retryCount,
    retryInitialInterval: Duration = this.retryInitialInterval,
    retryMaxInterval: Duration = this.retryMaxInterval,
    retryMultiplier: Double = this.retryMultiplier,
    retryRandomizationFactor: Double = this.retryRandomizationFactor,
    retryRandomizer: Random = this.retryRandomizer,
): QueryOptions {
    return QueryOptions(
        staleTime = staleTime,
        gcTime = gcTime,
        prefetchWindowTime = prefetchWindowTime,
        pauseDurationAfter = pauseDurationAfter,
        revalidateOnReconnect = revalidateOnReconnect,
        revalidateOnFocus = revalidateOnFocus,
        onError = onError,
        keepAliveTime = keepAliveTime,
        logger = logger,
        shouldRetry = shouldRetry,
        retryCount = retryCount,
        retryInitialInterval = retryInitialInterval,
        retryMaxInterval = retryMaxInterval,
        retryMultiplier = retryMultiplier,
        retryRandomizationFactor = retryRandomizationFactor,
        retryRandomizer = retryRandomizer
    )
}
