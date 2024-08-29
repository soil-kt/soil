// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlin.time.Duration

@OptIn(FlowPreview::class)
internal fun <T> Flow<T>.chunkedWithTimeout(
    size: Int,
    duration: Duration
): Flow<List<T>> {
    require(size > 0) { "'size' should be greater than 0" }
    require(duration > Duration.ZERO) { "'duration' should be greater than 0" }
    return flow {
        coroutineScope {
            val upstreamValues = produceIn(this)
            val chunks = ArrayList<T>(size)
            val ticker = MutableSharedFlow<Unit>(extraBufferCapacity = size)
            val tickerTimeout = ticker
                .debounce(duration)
                .produceIn(this)
            try {
                while (isActive) {
                    var isTimeout = false

                    select<Unit> {
                        upstreamValues.onReceive { value ->
                            chunks.add(value)
                            ticker.emit(Unit)
                        }
                        tickerTimeout.onReceive {
                            isTimeout = true
                        }
                    }

                    if (chunks.size == size || (isTimeout && chunks.isNotEmpty())) {
                        emit(chunks.toList())
                        chunks.clear()
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                if (chunks.isNotEmpty()) {
                    emit(chunks.toList())
                }
            } finally {
                tickerTimeout.cancel()
            }
        }
    }
}

internal fun <T> Flow<T>.retryWithExponentialBackoff(
    retryOptions: RetryOptions,
    onRetry: RetryCallback? = null
): Flow<T> {
    return retryWhen { cause, attempt ->
        if (attempt >= retryOptions.retryCount || !retryOptions.shouldRetry(cause)) {
            return@retryWhen false
        }

        val nextBackoff = retryOptions.calculateBackoffInterval(attempt.toInt())
        onRetry?.invoke(cause, attempt.toInt(), nextBackoff)

        delay(nextBackoff)
        return@retryWhen true
    }
}

internal fun <T> Flow<T>.toResultFlow(): Flow<Result<T>> {
    return this
        .map { value -> Result.success(value) }
        .catch { e -> emit(Result.failure(e)) }
}

/**
 * Returns null if an exception, including cancellation, occurs.
 */
internal suspend fun <T> Deferred<T>.awaitOrNull(): T? {
    return try {
        await()
    } catch (e: Throwable) {
        null
    }
}

@Suppress("FunctionName")
internal fun SharingStarted.Companion.WhileSubscribedAlt(
    stopTimeout: Duration,
    onSubscriptionCount: (Int) -> Unit
): SharingStarted = StartedWhileSubscribedAlt(stopTimeout, onSubscriptionCount)

@OptIn(ExperimentalCoroutinesApi::class)
private class StartedWhileSubscribedAlt(
    private val stopTimeout: Duration,
    private val onSubscriptionCount: (Int) -> Unit
) : SharingStarted {

    init {
        require(stopTimeout >= Duration.ZERO) { "stopTimeout cannot be negative" }
    }

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> = subscriptionCount
        .onEach { onSubscriptionCount(it) }
        .transformLatest { count ->
            if (count > 0) {
                emit(SharingCommand.START)
            } else {
                delay(stopTimeout)
                emit(SharingCommand.STOP)
            }
        }
        .dropWhile { it != SharingCommand.START }
        .distinctUntilChanged()

    override fun toString(): String {
        return "StartedWhileSubscribedAlt(stopTimeout=$stopTimeout)"
    }
}
