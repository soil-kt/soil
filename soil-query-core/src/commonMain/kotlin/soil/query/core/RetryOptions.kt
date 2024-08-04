// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Interface providing settings for retry logic.
 */
interface RetryOptions {

    /**
     * Specifies whether to retry the operation.
     */
    val shouldRetry: (Throwable) -> Boolean

    /**
     * The number of times to retry the operation.
     */
    val retryCount: Int

    /**
     * The initial interval for retrying the operation.
     */
    val retryInitialInterval: Duration

    /**
     * The maximum interval for retrying the operation.
     */
    val retryMaxInterval: Duration

    /**
     * The multiplier for the next interval.
     */
    val retryMultiplier: Double

    /**
     * The randomization factor for the next interval.
     */
    val retryRandomizationFactor: Double

    /**
     * The random number generator for the next interval.
     */
    val retryRandomizer: Random
}

/**
 * Generates an [RetryFn] for Exponential Backoff Strategy.
 */
fun <T> RetryOptions.exponentialBackOff(
    onRetry: RetryCallback? = null
) = RetryFn<T> { block ->
    var nextBackOff = retryInitialInterval
    repeat(retryCount) { count ->
        try {
            return@RetryFn block()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            if (!shouldRetry(t)) {
                throw t
            }
            onRetry?.invoke(t, count, nextBackOff)
        }
        val randomizedInterval = nextBackOff.times(
            retryRandomizer.nextDouble(
                1 - retryRandomizationFactor,
                1 + retryRandomizationFactor
            )
        )
        delay(randomizedInterval)
        nextBackOff = nextBackOff.times(retryMultiplier).coerceAtMost(retryMaxInterval)
    }
    block()
}
