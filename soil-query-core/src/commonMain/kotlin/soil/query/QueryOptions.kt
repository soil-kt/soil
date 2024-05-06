// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.ActorOptions
import soil.query.internal.LoggerFn
import soil.query.internal.LoggingOptions
import soil.query.internal.RetryOptions
import soil.query.internal.Retryable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * [QueryOptions] providing settings related to the internal behavior of an [Query].
 */
data class QueryOptions(

    /**
     * The duration after which the returned value of the fetch function block is considered stale.
     */
    val staleTime: Duration = Duration.ZERO,

    /**
     * The period during which the Key's return value, if not referenced anywhere, is temporarily cached in memory.
     */
    val gcTime: Duration = 5.minutes,

    /**
     * Maximum window time on prefetch processing.
     *
     * If this time is exceeded, the [kotlinx.coroutines.CoroutineScope] within the prefetch processing will terminate,
     * but the command processing within the Actor will continue as long as [keepAliveTime] is set.
     */
    val prefetchWindowTime: Duration = 1.seconds,

    /**
     * Determines whether query processing needs to be paused based on error.
     *
     * @see [shouldPause]
     */
    val pauseDurationAfter: ((Throwable) -> Duration?)? = null,

    /**
     * Automatically revalidate active [Query] when the network reconnects.
     *
     * **Note:**
     * This setting is only effective when [soil.query.internal.NetworkConnectivity] is available.
     */
    val revalidateOnReconnect: Boolean = true,

    /**
     * Automatically revalidate active [Query] when the window is refocused.
     *
     * **Note:**
     * This setting is only effective when [soil.query.internal.WindowVisibility] is available.
     */
    val revalidateOnFocus: Boolean = true,

    // ----- ActorOptions ----- //
    override val keepAliveTime: Duration = 5.seconds,

    // ----- LoggingOptions ----- //
    override val logger: LoggerFn? = null,

    // ----- RetryOptions ----- //
    override val shouldRetry: (Throwable) -> Boolean = { e ->
        e is Retryable && e.canRetry
    },
    override val retryCount: Int = 3,
    override val retryInitialInterval: Duration = 500.milliseconds,
    override val retryMaxInterval: Duration = 30.seconds,
    override val retryMultiplier: Double = 1.5,
    override val retryRandomizationFactor: Double = 0.5,
    override val retryRandomizer: Random = Random
) : ActorOptions, LoggingOptions, RetryOptions
