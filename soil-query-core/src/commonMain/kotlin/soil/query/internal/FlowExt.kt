// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
                .toReceiveChannel(this)
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

private fun <T> Flow<T>.toReceiveChannel(scope: CoroutineScope): ReceiveChannel<T> {
    val channel = Channel<T>(Channel.BUFFERED)
    scope.launch {
        try {
            collect { value -> channel.send(value) }
        } finally {
            channel.close()
        }
    }
    return channel
}
