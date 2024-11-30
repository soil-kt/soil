// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class EffectTest : UnitTest() {

    @Test
    fun testEffectContext() {
        val testPropertyKey = EffectPropertyKey<String>("test")
        val context = EffectContext(testPropertyKey to "value")
        val value = context[testPropertyKey]
        assertEquals("value", value)
    }

    @Test
    fun testEffectContext_keyNotFound() {
        val testPropertyKey = EffectPropertyKey<String>("test")
        val context = EffectContext()
        val t = assertFails {
            context[testPropertyKey]
        }
        assertEquals("test", t.message)
    }
}
