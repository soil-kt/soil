// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlin.time.Duration

/**
 * Functional interface for retry logic applied to queries or mutations within a command.
 *
 * @param T The return type of the function to which retry logic is applied.
 */
fun interface RetryFn<T> {

    /**
     * Executes the [block] function under retry control.
     *
     * @param block Function to which retry logic is applied.
     * @return The result of executing the [block] function.
     */
    suspend fun withRetry(block: suspend () -> T): T
}

/**
 * Interface to indicate whether an [Throwable] is retryable, provided as a default options.
 */
@Suppress("SpellCheckingInspection")
interface Retryable {

    /**
     * @return `true` only if retrying is possible.
     */
    val canRetry: Boolean
}

/**
 * Callback function to notify the execution of retry logic.
 */
typealias RetryCallback = (err: Throwable, count: Int, nextBackOff: Duration) -> Unit
