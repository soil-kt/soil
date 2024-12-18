// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ActorOptions
import soil.query.core.ErrorRecord
import soil.query.core.LoggerFn
import soil.query.core.LoggingOptions
import soil.query.core.RetryOptions
import soil.query.core.Retryable
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * [SubscriptionOptions] providing settings related to the internal behavior of an [Subscription].
 */
interface SubscriptionOptions : ActorOptions, LoggingOptions, RetryOptions {

    /**
     * The period during which the Key's return value, if not referenced anywhere, is temporarily cached in memory.
     */
    val gcTime: Duration

    /**
     * Determines whether two errors are equal.
     *
     * This function is used to determine whether a new error is identical to an existing error via [SubscriptionCommand].
     * If the errors are considered identical, [SubscriptionState.errorUpdatedAt] is not updated, and the existing error state is maintained.
     */
    val errorEquals: ((Throwable, Throwable) -> Boolean)?

    /**
     * Automatically restart active [Subscription] when the network reconnects.
     *
     * **Note:**
     * This setting is only effective when [soil.query.core.NetworkConnectivity] is available.
     */
    val restartOnReconnect: Boolean

    /**
     * Automatically restart active [Subscription] when the window is refocused.
     *
     * **Note:**
     * This setting is only effective when [soil.query.core.WindowVisibility] is available.
     */
    val restartOnFocus: Boolean

    /**
     * This callback function will be called if some mutation encounters an error.
     */
    val onError: ((ErrorRecord, SubscriptionModel<*>) -> Unit)?

    /**
     * Determines whether to suppress error information when relaying it using [soil.query.core.ErrorRelay].
     */
    val shouldSuppressErrorRelay: ((ErrorRecord, SubscriptionModel<*>) -> Boolean)?


    companion object Default : SubscriptionOptions {
        override val gcTime: Duration = 5.minutes
        override val errorEquals: ((Throwable, Throwable) -> Boolean)? = null
        override val restartOnReconnect: Boolean = true
        override val restartOnFocus: Boolean = true
        override val onError: ((ErrorRecord, SubscriptionModel<*>) -> Unit)? = null
        override val shouldSuppressErrorRelay: ((ErrorRecord, SubscriptionModel<*>) -> Boolean)? = null

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

/**
 * Creates a new [SubscriptionOptions] with the specified settings.
 *
 * @param gcTime The period during which the Key's return value, if not referenced anywhere, is temporarily cached in memory.
 * @param errorEquals Determines whether two errors are equal.
 * @param restartOnReconnect Automatically restart active [Subscription] when the network reconnects.
 * @param restartOnFocus Automatically restart active [Subscription] when the window is refocused.
 * @param onError This callback function will be called if some subscription encounters an error.
 * @param shouldSuppressErrorRelay Determines whether to suppress error information when relaying it using [soil.query.core.ErrorRelay].
 * @param keepAliveTime The duration to keep the actor alive after the last command is executed.
 * @param logger The logger function.
 * @param shouldRetry Determines whether to retry the command when an error occurs.
 * @param retryCount The number of times to retry the command.
 * @param retryInitialInterval The initial interval for exponential backoff.
 * @param retryMaxInterval The maximum interval for exponential backoff.
 * @param retryMultiplier The multiplier for exponential backoff.
 * @param retryRandomizationFactor The randomization factor for exponential backoff.
 * @param retryRandomizer The randomizer for exponential backoff.
 */
fun SubscriptionOptions(
    gcTime: Duration = SubscriptionOptions.gcTime,
    errorEquals: ((Throwable, Throwable) -> Boolean)? = SubscriptionOptions.errorEquals,
    restartOnReconnect: Boolean = SubscriptionOptions.restartOnReconnect,
    restartOnFocus: Boolean = SubscriptionOptions.restartOnFocus,
    onError: ((ErrorRecord, SubscriptionModel<*>) -> Unit)? = SubscriptionOptions.onError,
    shouldSuppressErrorRelay: ((ErrorRecord, SubscriptionModel<*>) -> Boolean)? = SubscriptionOptions.shouldSuppressErrorRelay,
    keepAliveTime: Duration = SubscriptionOptions.keepAliveTime,
    logger: LoggerFn? = SubscriptionOptions.logger,
    shouldRetry: (Throwable) -> Boolean = SubscriptionOptions.shouldRetry,
    retryCount: Int = SubscriptionOptions.retryCount,
    retryInitialInterval: Duration = SubscriptionOptions.retryInitialInterval,
    retryMaxInterval: Duration = SubscriptionOptions.retryMaxInterval,
    retryMultiplier: Double = SubscriptionOptions.retryMultiplier,
    retryRandomizationFactor: Double = SubscriptionOptions.retryRandomizationFactor,
    retryRandomizer: Random = SubscriptionOptions.retryRandomizer
): SubscriptionOptions {
    return object : SubscriptionOptions {
        override val gcTime: Duration = gcTime
        override val errorEquals: ((Throwable, Throwable) -> Boolean)? = errorEquals
        override val restartOnReconnect: Boolean = restartOnReconnect
        override val restartOnFocus: Boolean = restartOnFocus
        override val onError: ((ErrorRecord, SubscriptionModel<*>) -> Unit)? = onError
        override val shouldSuppressErrorRelay: ((ErrorRecord, SubscriptionModel<*>) -> Boolean)? =
            shouldSuppressErrorRelay
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
 * Copies the current [SubscriptionOptions] with the specified settings.
 */
fun SubscriptionOptions.copy(
    gcTime: Duration = this.gcTime,
    errorEquals: ((Throwable, Throwable) -> Boolean)? = this.errorEquals,
    restartOnReconnect: Boolean = this.restartOnReconnect,
    restartOnFocus: Boolean = this.restartOnFocus,
    onError: ((ErrorRecord, SubscriptionModel<*>) -> Unit)? = this.onError,
    shouldSuppressErrorRelay: ((ErrorRecord, SubscriptionModel<*>) -> Boolean)? = this.shouldSuppressErrorRelay,
    keepAliveTime: Duration = this.keepAliveTime,
    logger: LoggerFn? = this.logger,
    shouldRetry: (Throwable) -> Boolean = this.shouldRetry,
    retryCount: Int = this.retryCount,
    retryInitialInterval: Duration = this.retryInitialInterval,
    retryMaxInterval: Duration = this.retryMaxInterval,
    retryMultiplier: Double = this.retryMultiplier,
    retryRandomizationFactor: Double = this.retryRandomizationFactor,
    retryRandomizer: Random = this.retryRandomizer
): SubscriptionOptions {
    return SubscriptionOptions(
        gcTime = gcTime,
        errorEquals = errorEquals,
        restartOnReconnect = restartOnReconnect,
        restartOnFocus = restartOnFocus,
        onError = onError,
        shouldSuppressErrorRelay = shouldSuppressErrorRelay,
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
