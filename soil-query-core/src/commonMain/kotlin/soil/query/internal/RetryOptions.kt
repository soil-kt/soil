// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlin.time.Duration

interface RetryOptions {
    val shouldRetry: (Throwable) -> Boolean
    val retryCount: Int
    val retryInitialInterval: Duration
    val retryMaxInterval: Duration
    val retryMultiplier: Double
    val retryRandomizationFactor: Double
    val retryRandomizer: Random
}

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
