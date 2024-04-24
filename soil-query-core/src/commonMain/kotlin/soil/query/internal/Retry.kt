// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlin.time.Duration

fun interface RetryFn<T> {
    suspend fun withRetry(block: suspend () -> T): T
}

@Suppress("SpellCheckingInspection")
interface Retryable {
    val canRetry: Boolean
}

typealias RetryCallback = (err: Throwable, count: Int, nextBackOff: Duration) -> Unit
