// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationState
import soil.query.MutationStatus
import soil.query.buildMutationKey
import soil.query.compose.tooling.MutationPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.getOrThrow
import soil.query.core.isNone
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MutationObjectMapperTest : UnitTest() {

    @Test
    fun testToObject_idle() {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.initial() }
            }
        )
        val mutation = client.getMutation(key)
        val actual = with(MutationObjectMapper.Default) {
            mutation.state.value.toObject(mutation = mutation)
        }
        assertTrue(actual is MutationIdleObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(0, actual.mutatedCount)
        assertEquals(MutationStatus.Idle, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_pending() {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.pending() }
            }
        )
        val mutation = client.getMutation(key)
        val actual = with(MutationObjectMapper.Default) {
            mutation.state.value.toObject(mutation = mutation)
        }
        assertTrue(actual is MutationLoadingObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(0, actual.mutatedCount)
        assertEquals(MutationStatus.Pending, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_success() {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) {
                    MutationState.success(
                        data = "Hello, Mutation!",
                        dataUpdatedAt = 100,
                        mutatedCount = 1
                    )
                }
            }
        )
        val mutation = client.getMutation(key)
        val actual = with(MutationObjectMapper.Default) {
            mutation.state.value.toObject(mutation = mutation)
        }
        assertTrue(actual is MutationSuccessObject)
        assertEquals("Hello, Mutation!", actual.reply.getOrThrow())
        assertEquals(100, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(1, actual.mutatedCount)
        assertEquals(MutationStatus.Success, actual.status)
        assertNotNull(actual.data)
    }

    @Test
    fun testToObject_error() {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) {
                    MutationState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 200
                    )
                }
            }
        )
        val mutation = client.getMutation(key)
        val actual = with(MutationObjectMapper.Default) {
            mutation.state.value.toObject(mutation = mutation)
        }
        assertTrue(actual is MutationErrorObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(200, actual.errorUpdatedAt)
        assertEquals(0, actual.mutatedCount)
        assertEquals(MutationStatus.Failure, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_errorWithData() {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) {
                    MutationState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 200,
                        data = "Hello, Mutation!",
                        dataUpdatedAt = 100,
                        mutatedCount = 1
                    )
                }
            }
        )
        val mutation = client.getMutation(key)
        val actual = with(MutationObjectMapper.Default) {
            mutation.state.value.toObject(mutation = mutation)
        }
        assertTrue(actual is MutationErrorObject)
        assertEquals("Hello, Mutation!", actual.reply.getOrThrow())
        assertEquals(100, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(200, actual.errorUpdatedAt)
        assertEquals(1, actual.mutatedCount)
        assertEquals(MutationStatus.Failure, actual.status)
        assertNotNull(actual.data)
    }

    private class TestMutationKey : MutationKey<String, TestForm> by buildMutationKey(
        id = Id,
        mutate = { form ->
            "${form.name} - ${form.age}"
        }
    ) {
        object Id : MutationId<String, TestForm>(
            namespace = "test/mutation"
        )
    }

    private data class TestForm(
        val name: String,
        val age: Int
    )
}
