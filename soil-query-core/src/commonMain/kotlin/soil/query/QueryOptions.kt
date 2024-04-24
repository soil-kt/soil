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

data class QueryOptions(
    val staleTime: Duration = Duration.ZERO,
    val gcTime: Duration = 5.minutes,
    val prefetchWindowTime: Duration = 1.seconds,
    val pauseDurationAfter: ((Throwable) -> Duration?)? = null,
    val revalidateOnReconnect: Boolean = true,
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
