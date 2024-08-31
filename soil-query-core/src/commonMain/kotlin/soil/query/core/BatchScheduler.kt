// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Scheduler for batching tasks.
 */
interface BatchScheduler {

    /**
     * Post a task to the scheduler.
     */
    suspend fun post(task: BatchTask)
}

/**
 * Factory for creating [BatchScheduler] instances.
 */
fun interface BatchSchedulerFactory {

    /**
     * Create a new [BatchScheduler] instance.
     */
    fun create(scope: CoroutineScope): BatchScheduler

    companion object {
        /**
         * Create a new [BatchScheduler] factory with built-in scheduler implementation.
         *
         * @param dispatcher Coroutine dispatcher for the main thread.
         * @param interval Interval for batching tasks.
         * @param chunkSize Maximum number of tasks to execute in a batch.
         */
        fun default(
            dispatcher: CoroutineDispatcher = Dispatchers.Main,
            interval: Duration = 500.milliseconds,
            chunkSize: Int = 10
        ): BatchSchedulerFactory = BatchSchedulerFactory { scope ->
            DefaultBatchScheduler(scope, dispatcher, interval, chunkSize)
        }
    }
}

typealias BatchTask = () -> Unit

internal class DefaultBatchScheduler(
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    interval: Duration,
    chunkSize: Int
) : BatchScheduler {

    private val batchFlow: MutableSharedFlow<BatchTask> = MutableSharedFlow()

    init {
        batchFlow
            .chunkedWithTimeout(size = chunkSize, duration = interval)
            .onEach { tasks ->
                withContext(dispatcher) {
                    tasks.forEach { it() }
                }
            }
            .launchIn(scope)
    }

    override suspend fun post(task: BatchTask) {
        batchFlow.emit(task)
    }
}
