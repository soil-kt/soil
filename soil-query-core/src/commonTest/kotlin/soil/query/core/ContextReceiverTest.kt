// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContextReceiverTest : UnitTest() {

    @Test
    fun testBuilder() {
        val foo = FooClient()
        val bar = BarClient()
        val actual = TestReceiverBuilder().apply {
            fooClient = foo
            barClient = bar
        }.build()
        assertEquals(foo, actual.fooClient)
        assertEquals(bar, actual.barClient)
    }

    @Test
    fun testBuilder_unsetBar() {
        val foo = FooClient()
        val actual = TestReceiverBuilder().apply {
            fooClient = foo
        }.build()
        assertEquals(foo, actual.fooClient)
        assertNull(actual.barClient)
    }

    private class TestReceiver(
        context: Map<ContextPropertyKey<*>, Any>
    ) : ContextReceiverBase(context)

    private class TestReceiverBuilder(
    ) : ContextReceiverBuilderBase() {
        override fun build(): TestReceiver = TestReceiver(context)
    }

    private class FooClient

    private class BarClient

    private val ContextReceiver.fooClient: FooClient?
        get() = get(fooClientKey)

    private val ContextReceiver.barClient: BarClient?
        get() = get(barClientKey)

    private var ContextReceiverBuilder.fooClient: FooClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(fooClientKey, value)

    private var ContextReceiverBuilder.barClient: BarClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(barClientKey, value)

    companion object {
        private val fooClientKey = ContextPropertyKey<FooClient>()
        private val barClientKey = ContextPropertyKey<BarClient>()
    }
}
