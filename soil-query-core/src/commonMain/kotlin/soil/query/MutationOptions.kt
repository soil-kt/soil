// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.ActorOptions
import soil.query.internal.LoggerFn
import soil.query.internal.LoggingOptions
import soil.query.internal.RetryOptions
import soil.query.internal.UniqueId
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * [MutationOptions] providing settings related to the internal behavior of an [Mutation].
 */
interface MutationOptions : ActorOptions, LoggingOptions, RetryOptions {

    /**
     * Only allows mutate to execute once while active (until reset).
     */
    val isOneShot: Boolean

    /**
     * Requires revision match as a precondition for executing mutate.
     */
    val isStrictMode: Boolean

    /**
     * This callback function will be called if some mutation encounters an error.
     */
    val onError: ((Throwable, MutationModel<*>, UniqueId) -> Unit)?

    /**
     * Whether the query side effect should be synchronous. If true, side effect will be executed synchronously.
     */
    val shouldExecuteEffectSynchronously: Boolean

    companion object Default : MutationOptions {
        override val isOneShot: Boolean = false
        override val isStrictMode: Boolean = false
        override val onError: ((Throwable, MutationModel<*>, UniqueId) -> Unit)? = null
        override val shouldExecuteEffectSynchronously: Boolean = false

        // ----- ActorOptions ----- //
        override val keepAliveTime: Duration = 5.seconds

        // ----- LoggingOptions ----- //
        override val logger: LoggerFn? = null

        // ----- RetryOptions ----- //
        override val shouldRetry: (Throwable) -> Boolean = { false }
        override val retryCount: Int = 3
        override val retryInitialInterval: Duration = 500.milliseconds
        override val retryMaxInterval: Duration = 30.seconds
        override val retryMultiplier: Double = 1.5
        override val retryRandomizationFactor: Double = 0.5
        override val retryRandomizer: Random = Random
    }
}

fun MutationOptions(
    isOneShot: Boolean = MutationOptions.isOneShot,
    isStrictMode: Boolean = MutationOptions.isStrictMode,
    onError: ((Throwable, MutationModel<*>, UniqueId) -> Unit)? = MutationOptions.onError,
    shouldExecuteEffectSynchronously: Boolean = MutationOptions.shouldExecuteEffectSynchronously,
    keepAliveTime: Duration = MutationOptions.keepAliveTime,
    logger: LoggerFn? = MutationOptions.logger,
    shouldRetry: (Throwable) -> Boolean = MutationOptions.shouldRetry,
    retryCount: Int = MutationOptions.retryCount,
    retryInitialInterval: Duration = MutationOptions.retryInitialInterval,
    retryMaxInterval: Duration = MutationOptions.retryMaxInterval,
    retryMultiplier: Double = MutationOptions.retryMultiplier,
    retryRandomizationFactor: Double = MutationOptions.retryRandomizationFactor,
    retryRandomizer: Random = MutationOptions.retryRandomizer
): MutationOptions {
    return object : MutationOptions {
        override val isOneShot: Boolean = isOneShot
        override val isStrictMode: Boolean = isStrictMode
        override val onError: ((Throwable, MutationModel<*>, UniqueId) -> Unit)? = onError
        override val shouldExecuteEffectSynchronously: Boolean = shouldExecuteEffectSynchronously
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

fun MutationOptions.copy(
    isOneShot: Boolean = this.isOneShot,
    isStrictMode: Boolean = this.isStrictMode,
    onError: ((Throwable, MutationModel<*>, UniqueId) -> Unit)? = this.onError,
    shouldExecuteEffectSynchronously: Boolean = this.shouldExecuteEffectSynchronously,
    keepAliveTime: Duration = this.keepAliveTime,
    logger: LoggerFn? = this.logger,
    shouldRetry: (Throwable) -> Boolean = this.shouldRetry,
    retryCount: Int = this.retryCount,
    retryInitialInterval: Duration = this.retryInitialInterval,
    retryMaxInterval: Duration = this.retryMaxInterval,
    retryMultiplier: Double = this.retryMultiplier,
    retryRandomizationFactor: Double = this.retryRandomizationFactor,
    retryRandomizer: Random = this.retryRandomizer
): MutationOptions {
    return MutationOptions(
        isOneShot = isOneShot,
        isStrictMode = isStrictMode,
        onError = onError,
        shouldExecuteEffectSynchronously = shouldExecuteEffectSynchronously,
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
