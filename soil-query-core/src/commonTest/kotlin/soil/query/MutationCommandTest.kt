// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import soil.query.core.Effect
import soil.query.core.ErrorRelay
import soil.query.core.Marker
import soil.query.core.RetryFn
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MutationCommandTest : UnitTest() {

    @Test
    fun testShouldMutate() {
        val ctx = TestMutationContext()
        assertTrue(ctx.shouldMutate(revision = ctx.state.revision))
    }

    @Test
    fun testShouldMutate_withOneShotAndMutated() {
        val ctx = TestMutationContext(
            options = MutationOptions(isOneShot = true),
            state = MutationState.success("Test")
        )
        assertFalse(ctx.shouldMutate(revision = ctx.state.revision))
    }

    @Test
    fun testShouldMutate_withStrictModeAndDifferentRevision() {
        val ctx = TestMutationContext(
            options = MutationOptions(isStrictMode = true),
            state = MutationState.success("Test")
        )
        assertFalse(ctx.shouldMutate(ctx.state.revision + "difference"))
    }

    @Test
    fun testShouldMutate_withPendingState() {
        val ctx = TestMutationContext(
            state = MutationState.pending()
        )
        assertFalse(ctx.shouldMutate(ctx.state.revision))
    }

    @Test
    fun testMutate_success() = runTest {
        val ctx = TestMutationContext()
        val key = TestMutationKey(mockMutate = { "test-data" })
        val job = launch {
            val result = ctx.mutate(key, "variable-data", TestNonRetry)
            assertTrue(result.isSuccess)
            assertEquals("test-data", result.getOrThrow())
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testMutate_failure() = runTest {
        val ctx = TestMutationContext()
        val key = TestMutationKey(mockMutate = { error("Test") })
        val job = launch {
            val result = ctx.mutate(key, "variable-data", TestNonRetry)
            assertTrue(result.isFailure)
            assertEquals("Test", result.exceptionOrNull()?.message)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testMutate_cancel() = runTest {
        val ctx = TestMutationContext()
        val key = TestMutationKey(mockMutate = { throw CancellationException("Test") })
        val job = launch {
            ctx.mutate(key, "variable-data", TestNonRetry)
        }
        job.join()
        assertTrue(job.isCancelled)
    }

    @Test
    fun testDispatchMutateResult_success() = runTest {
        var dispatchedAction: MutationAction<String>? = null
        val ctx = TestMutationContext(
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestMutationKey(mockMutate = { "test-data" })
        val marker = Marker.None
        var callbackResult: Result<String>? = null
        val callback: MutationCallback<String> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchMutateResult(key, "variable-data", marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is MutationAction.MutateSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as MutationAction.MutateSuccess).data
        assertEquals("test-data", actionData)
    }

    @Test
    fun testDispatchMutateResult_failure() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: MutationAction<String>? = null
        val ctx = TestMutationContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestMutationKey(mockMutate = { error("Test") })
        val marker = Marker.None
        var callbackResult: Result<String>? = null
        val callback: MutationCallback<String> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchMutateResult(key, "variable-data", marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is MutationAction.MutateFailure)
        assertTrue(callbackResult?.isFailure == true)
        val job2 = launch {
            errorRelay.receiveAsFlow().test {
                assertEquals(TestMutationKey.Id, awaitItem().keyId)
            }
        }
        job2.join()
    }

    private object TestNonRetry : RetryFn<String> {
        override suspend fun withRetry(block: suspend () -> String): String = block()
    }

    private class TestMutationKey(
        private val mockMutate: suspend (String) -> String = { "test-data" },
        private val mockMutationContentEquals: MutationContentEquals<String>? = null,
        private val mockMutateEffect: Effect? = null
    ) : MutationKey<String, String> by buildMutationKey(
        id = Id,
        mutate = { mockMutate(it) }
    ) {
        override val contentEquals: MutationContentEquals<String>? get() = mockMutationContentEquals
        override fun onMutateEffect(variable: String, data: String): Effect? = mockMutateEffect

        object Id : MutationId<String, String>(
            namespace = "test-mutation"
        )
    }

    private class TestMutationContext(
        override val state: MutationModel<String> = MutationState.initial(),
        override val dispatch: MutationDispatch<String> = {},
        override val options: MutationOptions = MutationOptions(),
        override val relay: MutationErrorRelay? = null,
        override val receiver: MutationReceiver = MutationReceiver {},
        override val notifier: MutationNotifier = MutationNotifier { Job() }
    ) : MutationCommand.Context<String>
}
