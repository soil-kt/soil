// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import soil.query.core.ErrorRelay
import soil.query.core.Marker
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubscriptionCommandTest : UnitTest() {

    @Test
    fun testDispatchResult_success() = runTest {
        var dispatchedAction: SubscriptionAction<String>? = null
        val ctx = TestSubscriptionContext(
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestSubscriptionKey(mockRecoverData = null)
        val marker = Marker.None
        val result = Result.success("test-data")

        val job = launch {
            ctx.dispatchResult(key, result, marker)
        }
        job.join()
        assertTrue(dispatchedAction is SubscriptionAction.ReceiveSuccess)

        val actionData = (dispatchedAction as SubscriptionAction.ReceiveSuccess).data
        assertEquals("test-data", actionData)
    }

    @Test
    fun testDispatchResult_failure() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: SubscriptionAction<String>? = null
        val ctx = TestSubscriptionContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestSubscriptionKey(mockRecoverData = null)
        val marker = Marker.None
        val error = Exception("Test")
        val result = Result.failure<String>(error)

        val job = launch {
            ctx.dispatchResult(key, result, marker)
        }
        job.join()
        assertTrue(dispatchedAction is SubscriptionAction.ReceiveFailure)
        val actionError = (dispatchedAction as SubscriptionAction.ReceiveFailure).error
        assertEquals("Test", actionError.message)

        val job2 = launch {
            errorRelay.receiveAsFlow().test {
                assertEquals(TestSubscriptionKey.Id, awaitItem().keyId)
            }
        }
        job2.join()
    }

    @Test
    fun testDispatchResult_failure_withRecoverData() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: SubscriptionAction<String>? = null
        val ctx = TestSubscriptionContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestSubscriptionKey(mockRecoverData = { "recovered-data" })
        val marker = Marker.None
        val error = Exception("Test")
        val result = Result.failure<String>(error)

        val job = launch {
            ctx.dispatchResult(key, result, marker)
        }
        job.join()
        assertTrue(dispatchedAction is SubscriptionAction.ReceiveSuccess)
        val actionData = (dispatchedAction as SubscriptionAction.ReceiveSuccess).data
        assertEquals("recovered-data", actionData)
    }

    private class TestSubscriptionKey(
        private val mockSubscribe: SubscriptionReceiver.() -> Flow<String> = { flow { emit("Test") } },
        private val mockRecoverData: SubscriptionRecoverData<String>?
    ) : SubscriptionKey<String> by buildSubscriptionKey(
        id = Id,
        subscribe = { mockSubscribe() }
    ) {
        override fun onRecoverData(): SubscriptionRecoverData<String>? = mockRecoverData

        object Id : SubscriptionId<String>(
            namespace = "test-subscription"
        )
    }

    private class TestSubscriptionContext(
        override val state: SubscriptionModel<String> = SubscriptionState.initial(),
        override val dispatch: SubscriptionDispatch<String> = {},
        override val options: SubscriptionOptions = SubscriptionOptions(),
        override val relay: SubscriptionErrorRelay? = null,
        override val restart: SubscriptionRestart = {}
    ) : SubscriptionCommand.Context<String>
}
