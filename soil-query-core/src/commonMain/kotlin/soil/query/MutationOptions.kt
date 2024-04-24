// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.ActorOptions
import soil.query.internal.LoggerFn
import soil.query.internal.LoggingOptions
import soil.query.internal.RetryOptions
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


data class MutationOptions(
    // NOTE: Only allows mutate to execute once while active (until reset).
    val isOneShot: Boolean = false,
    // NOTE: Requires revision match as a precondition for executing mutate.
    val isStrictMode: Boolean = false,

    // ----- ActorOptions ----- //
    override val keepAliveTime: Duration = 5.seconds,

    // ----- LoggingOptions ----- //
    override val logger: LoggerFn? = null,

    // ----- RetryOptions ----- //
    override val shouldRetry: (Throwable) -> Boolean = { false },
    override val retryCount: Int = 3,
    override val retryInitialInterval: Duration = 500.milliseconds,
    override val retryMaxInterval: Duration = 30.seconds,
    override val retryMultiplier: Double = 1.5,
    override val retryRandomizationFactor: Double = 0.5,
    override val retryRandomizer: Random = Random
) : ActorOptions, LoggingOptions, RetryOptions
