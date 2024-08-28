// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class BatchSchedulerTest : UnitTest() {

    @Test
    fun testDefault_chunkSizeTrigger() = runTest {
        val scheduler = BatchScheduler.default(
            dispatcher = StandardTestDispatcher(testScheduler),
            interval = 3.seconds,
            chunkSize = 3
        )
        val scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        scheduler.start(scope)
        val task = TestBatchTask()

        scheduler.post(task)
        scheduler.post(task)

        advanceTimeBy(500.milliseconds)
        assertEquals(0, task.executedCount)

        scheduler.post(task)

        advanceTimeBy(500.milliseconds)
        assertEquals(3, task.executedCount)
    }

    @Test
    fun testDefault_intervalTrigger() = runTest {
        val scheduler = BatchScheduler.default(
            dispatcher = StandardTestDispatcher(testScheduler),
            interval = 3.seconds,
            chunkSize = 3
        )
        val scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        scheduler.start(scope)
        val task = TestBatchTask()

        scheduler.post(task)
        scheduler.post(task)

        advanceTimeBy(1.seconds)
        assertEquals(0, task.executedCount)

        advanceTimeBy(3.seconds)
        assertEquals(2, task.executedCount)
    }

    private class TestBatchTask : BatchTask {
        var executedCount: Int = 0
            private set

        override fun invoke() {
            executedCount++
        }
    }
}
