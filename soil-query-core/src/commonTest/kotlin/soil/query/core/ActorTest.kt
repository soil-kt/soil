// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ActorTest : UnitTest() {

    @Test
    fun testOne() = runTest {
        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = backgroundScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(actor.hasAttachedInstances(), false)
        assertEquals(0, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.attach("test#0")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()
        assertEquals(1, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.detach("test#0")
        assertEquals(actor.hasAttachedInstances(), false)

        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)

        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.count)
    }

    @Test
    fun testMany() = runTest {
        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = backgroundScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(actor.hasAttachedInstances(), false)
        assertEquals(0, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.attach("test#1")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()

        assertEquals(1, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.attach("test#2")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()

        assertEquals(1, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.detach("test#1")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()

        assertEquals(1, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.detach("test#2")
        assertEquals(actor.hasAttachedInstances(), false)

        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.count)
    }

    @Test
    fun testActorScopeCanceled() = runTest {
        val actorScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val blockHandler = TestBlockHandler()
        val timeoutHandler = TestTimeoutHandler()
        val actor = ActorBlockRunner(
            scope = actorScope,
            options = TestActorOptions(),
            onTimeout = timeoutHandler::onTimeout,
            block = blockHandler::handle
        )

        assertEquals(actor.hasAttachedInstances(), false)
        assertEquals(0, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.attach("test#1")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()

        assertEquals(1, blockHandler.count)
        assertEquals(0, timeoutHandler.count)

        actor.detach("test#1")
        assertEquals(actor.hasAttachedInstances(), false)

        // Wait for the actor to be canceled. (keepAliveTime = 5.seconds)
        advanceTimeBy(6.seconds)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.count)

        actorScope.cancel()
        advanceUntilIdle()

        actor.attach("test#2")
        assertEquals(actor.hasAttachedInstances(), true)

        runCurrent()

        // Unchanged (already canceled)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.count)

        actor.detach("test#2")

        advanceTimeBy(6.seconds)
        assertEquals(1, blockHandler.count)
        assertEquals(1, timeoutHandler.count)
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
        var count: Int = 0
            private set

        fun onTimeout() {
            count++
        }
    }

    private class TestActorOptions(
        override val keepAliveTime: Duration = 5.seconds
    ) : ActorOptions
}
