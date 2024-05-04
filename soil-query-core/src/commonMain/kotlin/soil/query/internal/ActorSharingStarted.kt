// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration

/**
 * Implementation of [SharingStarted] for Actor's custom [SharingStarted.WhileSubscribed] implementations.
 *
 * @see [ActorOptions.newSharingStarted]
 */
class ActorSharingStarted(
    keepAliveTime: Duration,
    private val onActive: ActorCallback? = null,
    private val onInactive: ActorCallback? = null
) : SharingStarted {

    private val whileSubscribed = SharingStarted.WhileSubscribed(
        stopTimeoutMillis = keepAliveTime.inWholeMilliseconds,
        replayExpirationMillis = Duration.INFINITE.inWholeMilliseconds
    )

    override fun command(
        subscriptionCount: StateFlow<Int>
    ): Flow<SharingCommand> = whileSubscribed.command(subscriptionCount)
        .onEach {
            when (it) {
                SharingCommand.START -> onActive?.invoke()
                SharingCommand.STOP -> onInactive?.invoke()
                else -> Unit
            }
        }
}

/**
 * Callback handler to notify based on the active state of the Actor.
 */
typealias ActorCallback = () -> Unit
