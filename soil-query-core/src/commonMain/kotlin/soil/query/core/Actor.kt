// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * An actor represents a launch entry point for processing a query or mutation command.
 */
interface Actor {

    /**
     * Attaches an instance to the actor.
     *
     * **Note:**
     * Currently, This function must be called from the main(UI) thread.
     * If you call it from a thread other than the main thread, the internal state value will be out of sync.
     *
     */
    fun attach(iid: InstanceId)

    /**
     * Detaches an instance from the actor.
     *
     * **Note:**
     * Currently, This function must be called from the main(UI) thread.
     * If you call it from a thread other than the main thread, the internal state value will be out of sync.*
     */
    fun detach(iid: InstanceId)

    /**
     * Returns whether the actor has attached instances.
     */
    fun hasAttachedInstances(): Boolean
}

typealias InstanceId = String

internal class ActorBlockRunner(
    private val scope: CoroutineScope,
    private val options: ActorOptions,
    private val onTimeout: () -> Unit,
    private val block: suspend () -> Unit
) : Actor {

    private var runningJob: Job? = null
    private var cancellationJob: Job? = null
    private var currentAttachedIds: Set<InstanceId> = emptySet()

    override fun hasAttachedInstances(): Boolean {
        return currentAttachedIds.isNotEmpty()
    }

    override fun attach(iid: InstanceId) {
        cancellationJob?.cancel()
        cancellationJob = null
        if (currentAttachedIds.isEmpty()) {
            start()
        }
        currentAttachedIds += iid
    }

    override fun detach(iid: InstanceId) {
        currentAttachedIds -= iid
        if (currentAttachedIds.isEmpty()) {
            stop()
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
            onTimeout()
        }
    }
}
