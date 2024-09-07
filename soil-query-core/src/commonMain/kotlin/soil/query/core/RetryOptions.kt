// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
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
    repeat(retryCount) { attempt ->
        val cause = try {
            return@RetryFn block()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            if (!shouldRetry(t)) {
                throw t
            }
            t
        }

        val nextBackOff = calculateBackoffInterval(attempt)
        onRetry?.invoke(cause, attempt, nextBackOff)

        delay(nextBackOff)
    }
    block()
}

fun RetryOptions.calculateBackoffInterval(attempt: Int): Duration {
    val exponentialBackoff = retryInitialInterval * retryMultiplier.pow(attempt.toDouble())
    val randomizedBackoff = exponentialBackoff * retryRandomizer.nextDouble(
        1.0 - retryRandomizationFactor,
        1.0 + retryRandomizationFactor
    )
    return randomizedBackoff.coerceAtMost(retryMaxInterval)
}
