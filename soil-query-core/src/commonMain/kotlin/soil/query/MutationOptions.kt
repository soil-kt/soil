// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ActorOptions
import soil.query.core.ErrorRecord
import soil.query.core.LoggerFn
import soil.query.core.LoggingOptions
import soil.query.core.RetryOptions
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
     * Determines whether two errors are equal.
     *
     * This function is used to determine whether a new error is identical to an existing error via [MutationCommand].
     * If the errors are considered identical, [MutationState.errorUpdatedAt] is not updated, and the existing error state is maintained.
     */
    val errorEquals: ((Throwable, Throwable) -> Boolean)?

    /**
     * This callback function will be called if some mutation encounters an error.
     */
    val onError: ((ErrorRecord, MutationModel<*>) -> Unit)?

    /**
     * Determines whether to suppress error information when relaying it using [soil.query.core.ErrorRelay].
     */
    val shouldSuppressErrorRelay: ((ErrorRecord, MutationModel<*>) -> Boolean)?

    /**
     * Whether the query side effect should be synchronous. If true, side effect will be executed synchronously.
     */
    val shouldExecuteEffectSynchronously: Boolean

    companion object Default : MutationOptions {
        override val isOneShot: Boolean = false
        override val isStrictMode: Boolean = false
        override val errorEquals: ((Throwable, Throwable) -> Boolean)? = null
        override val onError: ((ErrorRecord, MutationModel<*>) -> Unit)? = null
        override val shouldSuppressErrorRelay: ((ErrorRecord, MutationModel<*>) -> Boolean)? = null
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

/**
 * Creates a new [MutationOptions] with the specified settings.
 *
 * @param isOneShot Only allows mutate to execute once while active (until reset).
 * @param isStrictMode Requires revision match as a precondition for executing mutate.
 * @param errorEquals Determines whether two errors are equal.
 * @param onError This callback function will be called if some mutation encounters an error.
 * @param shouldSuppressErrorRelay Determines whether to suppress error information when relaying it using [soil.query.core.ErrorRelay].
 * @param shouldExecuteEffectSynchronously Whether the query side effect should be synchronous.
 * @param keepAliveTime The duration to keep the actor alive after the last message is processed.
 * @param logger The logger function to use for logging.
 * @param shouldRetry The predicate function to determine whether to retry on a given exception.
 * @param retryCount The maximum number of retry attempts.
 * @param retryInitialInterval The initial interval for exponential backoff.
 * @param retryMaxInterval The maximum interval for exponential backoff.
 * @param retryMultiplier The multiplier for exponential backoff.
 * @param retryRandomizationFactor The randomization factor for exponential backoff.
 * @param retryRandomizer The random number generator for exponential backoff.
 */
fun MutationOptions(
    isOneShot: Boolean = MutationOptions.isOneShot,
    isStrictMode: Boolean = MutationOptions.isStrictMode,
    errorEquals: ((Throwable, Throwable) -> Boolean)? = MutationOptions.errorEquals,
    onError: ((ErrorRecord, MutationModel<*>) -> Unit)? = MutationOptions.onError,
    shouldSuppressErrorRelay: ((ErrorRecord, MutationModel<*>) -> Boolean)? = MutationOptions.shouldSuppressErrorRelay,
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
        override val errorEquals: ((Throwable, Throwable) -> Boolean)? = errorEquals
        override val onError: ((ErrorRecord, MutationModel<*>) -> Unit)? = onError
        override val shouldSuppressErrorRelay: ((ErrorRecord, MutationModel<*>) -> Boolean)? = shouldSuppressErrorRelay
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

/**
 * Copies the current [MutationOptions] with the specified settings.
 */
fun MutationOptions.copy(
    isOneShot: Boolean = this.isOneShot,
    isStrictMode: Boolean = this.isStrictMode,
    errorEquals: ((Throwable, Throwable) -> Boolean)? = this.errorEquals,
    onError: ((ErrorRecord, MutationModel<*>) -> Unit)? = this.onError,
    shouldSuppressErrorRelay: ((ErrorRecord, MutationModel<*>) -> Boolean)? = this.shouldSuppressErrorRelay,
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
        errorEquals = errorEquals,
        onError = onError,
        shouldSuppressErrorRelay = shouldSuppressErrorRelay,
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
