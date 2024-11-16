// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SwrReceiverPlusTest : UnitTest() {

    @Test
    fun testBuilder() {
        val foo = FooClient()
        val bar = BarClient()
        val baz = BazClient()
        val actual = SwrReceiverBuilderPlusImpl().apply {
            fooClient = foo
            barClient = bar
            bazClient = baz
        }.build()
        assertEquals(foo, actual.fooClient)
        assertEquals(bar, actual.barClient)
        assertEquals(baz, actual.bazClient)
    }

    private class FooClient

    private class BarClient

    private class BazClient

    private val QueryReceiver.fooClient: FooClient?
        get() = get(fooClientKey)

    private val MutationReceiver.barClient: BarClient?
        get() = get(barClientKey)

    private val SubscriptionReceiver.bazClient: BazClient?
        get() = get(bazClientKey)

    private var QueryReceiverBuilder.fooClient: FooClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(fooClientKey, value)

    private var MutationReceiverBuilder.barClient: BarClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(barClientKey, value)

    private var SubscriptionReceiverBuilder.bazClient: BazClient
        get() = error("You cannot retrieve a builder property directly.")
        set(value) = set(bazClientKey, value)

    companion object {
        private val fooClientKey = ContextPropertyKey<FooClient>()
        private val barClientKey = ContextPropertyKey<BarClient>()
        private val bazClientKey = ContextPropertyKey<BazClient>()
    }
}
