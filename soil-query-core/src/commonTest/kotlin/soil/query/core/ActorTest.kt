// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ActorTest : UnitTest() {

    @Test
    fun testLaunchIn_one() = runTest {
        val uiDispatcher = UnconfinedTestDispatcher(testScheduler)

        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = backgroundScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(0, actor.seq)
        assertEquals(0, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        val scope1 = CoroutineScope(uiDispatcher)
        val job1 = actor.launchIn(scope1)
        yield()

        assertEquals(1, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        job1.cancel()
        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)

        assertEquals(1, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.seq)
    }

    @Test
    fun testLaunchIn_many() = runTest {
        val uiDispatcher = UnconfinedTestDispatcher(testScheduler)

        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = backgroundScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(0, actor.seq)
        assertEquals(0, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        val scope1 = CoroutineScope(uiDispatcher)
        val job1 = actor.launchIn(scope1)
        yield()

        assertEquals(1, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        val scope2 = CoroutineScope(uiDispatcher)
        val job2 = actor.launchIn(scope2)
        yield()

        assertEquals(2, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        job1.cancel()
        yield()

        assertEquals(2, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        job2.cancel()

        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)
        assertEquals(2, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(2, timeoutHandler.seq)
    }

    @Test
    fun testLaunchIn_actorCanceled() = runTest {
        val uiDispatcher = UnconfinedTestDispatcher(testScheduler)

        val actorScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = actorScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(0, actor.seq)
        assertEquals(0, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        val scope1 = CoroutineScope(uiDispatcher)
        val job1 = actor.launchIn(scope1)
        yield()

        assertEquals(1, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(-1, timeoutHandler.seq)

        job1.cancel()

        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)
        assertEquals(1, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.seq)

        actorScope.cancel()
        advanceUntilIdle()

        val scope2 = CoroutineScope(uiDispatcher)
        val job2 = actor.launchIn(scope2)
        yield()

        assertEquals(2, actor.seq)
        // Unchanged (already canceled)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.seq)

        job2.cancel()

        advanceTimeBy(6.seconds)
        assertEquals(2, actor.seq)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.seq)
    }

    private class TestBlockHandler {

        val dummyFlow = MutableStateFlow(0)

        var count: Int = 0
            private set

        suspend fun handle() {
            count++
            dummyFlow.collect { }
        }
    }

    private class TestTimeoutHandler {
        var seq: ActorSequenceNumber = -1
            private set

        fun onTimeout(seq: ActorSequenceNumber) {
            this.seq = seq
        }
    }

    private class TestActorOptions(
        override val keepAliveTime: Duration = 5.seconds
    ) : ActorOptions
}
