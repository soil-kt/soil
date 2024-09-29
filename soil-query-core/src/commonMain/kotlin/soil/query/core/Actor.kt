// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Duration

/**
 * An actor represents a launch entry point for processing a query or mutation command.
 */
interface Actor {

    /**
     * Launches the actor.
     *
     * The Actor will continue to run as long as any one of the invoked [scope]s is valid.
     *
     * **Note:**
     * Currently, This function must be called from the main(UI) thread.
     * If you call it from a thread other than the main thread, the internal counter value will be out of sync.
     *
     * @param scope The scope in which the actor will run
     */
    fun launchIn(scope: CoroutineScope): Job
}

internal typealias ActorSequenceNumber = String

internal class ActorBlockRunner(
    private val id: String = uuid(),
    private val scope: CoroutineScope,
    private val options: ActorOptions,
    private val onTimeout: (ActorSequenceNumber) -> Unit,
    private val block: suspend () -> Unit
) : Actor {

    val seq: ActorSequenceNumber
        get() = "$id#$versionCounter"

    private var versionCounter: Int = 0
    private var activeCounter: Int = 0
    private var hasActiveScope: Boolean = false
    private var runningJob: Job? = null
    private var cancellationJob: Job? = null

    override fun launchIn(scope: CoroutineScope): Job {
        versionCounter++
        return scope.launch(start = CoroutineStart.UNDISPATCHED) {
            cancellationJob?.cancelAndJoin()
            cancellationJob = null
            suspendCancellableCoroutine { continuation ->
                activeCounter++
                if (!hasActiveScope && activeCounter > 0) {
                    hasActiveScope = true
                    start()
                }
                continuation.invokeOnCancellation {
                    activeCounter--
                    if (hasActiveScope && activeCounter <= 0) {
                        hasActiveScope = false
                        stop()
                    }
                }
            }
        }
    }

    private fun start() {
        if (runningJob?.isActive == true) {
            return
        }
        runningJob = scope.launch {
            block()
        }
    }

    private fun stop() {
        if (cancellationJob?.isActive == true) {
            return
        }
        cancellationJob = scope.launch {
            if (options.keepAliveTime >= Duration.ZERO) {
                delay(options.keepAliveTime)
            }
            onTimeout(seq)
        }
    }
}
