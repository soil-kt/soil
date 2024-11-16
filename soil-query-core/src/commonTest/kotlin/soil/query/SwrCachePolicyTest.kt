// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ContextPropertyKey
import soil.query.core.ContextReceiver
import soil.query.core.ContextReceiverBuilder
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SwrCachePolicyTest : UnitTest() {

    @Test
    fun testFactory() {
        val actual = SwrCachePolicy(SwrCacheScope())
        assertEquals(null, actual.queryReceiver.fooClient)
        assertEquals(null, actual.queryReceiver.barClient)
        assertEquals(null, actual.mutationReceiver.fooClient)
        assertEquals(null, actual.mutationReceiver.barClient)
    }

    @Test
    fun testFactory_withReceiver() {
        val foo = FooClient()
        val bar = BarClient()
        val actual = SwrCachePolicy(SwrCacheScope()) {
            fooClient = foo
            barClient = bar
        }
        assertEquals(foo, actual.queryReceiver.fooClient)
        assertEquals(bar, actual.queryReceiver.barClient)
        assertEquals(foo, actual.mutationReceiver.fooClient)
        assertEquals(bar, actual.mutationReceiver.barClient)
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
