// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBuilder
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionReceiverTest : UnitTest() {

    @Test
    fun testBuilder() {
        val foo = FooClient()
        val bar = BarClient()
        val actual = SubscriptionReceiver {
            fooClient = foo
            barClient = bar
        }
        assertEquals(foo, actual.fooClient)
        assertEquals(bar, actual.barClient)
    }

    private class FooClient

    private class BarClient

    private val ContextReceiver.fooClient: FooClient?
        get() = get(fooClientKey)

    private val SubscriptionReceiver.barClient: BarClient?
        get() = get(barClientKey)

    private var ContextReceiverBuilder.fooClient: FooClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(fooClientKey, value)

    private var SubscriptionReceiverBuilder.barClient: BarClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(barClientKey, value)

    companion object {
        private val fooClientKey = ContextPropertyKey<FooClient>()
        private val barClientKey = ContextPropertyKey<BarClient>()
    }
}
