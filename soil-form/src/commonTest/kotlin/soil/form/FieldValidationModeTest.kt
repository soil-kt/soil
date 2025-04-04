// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FieldValidationModeTest : UnitTest() {

    @Test
    fun fieldValidationStrategy() {
        val strategy = FieldValidationStrategy()

        assertEquals(FieldValidationMode.Blur, strategy.initial)
        assertEquals(FieldValidationMode.Blur, strategy.next(FieldValidationMode.Mount, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Mount, false))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Blur, false))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Blur, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Change, false))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Change, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Blur, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Blur, false))
    }

    @Test
    fun fieldValidationStrategy_withCustomBehavior() {
        val strategy = FieldValidationStrategy(initial = FieldValidationMode.Mount) { current, isValid ->
            when (current) {
                FieldValidationMode.Mount -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
                FieldValidationMode.Blur -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
                FieldValidationMode.Change -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
                FieldValidationMode.Submit -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
            }
        }

        assertEquals(FieldValidationMode.Mount, strategy.initial)
        assertEquals(FieldValidationMode.Blur, strategy.next(FieldValidationMode.Mount, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Mount, false))
        assertEquals(FieldValidationMode.Blur, strategy.next(FieldValidationMode.Blur, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Blur, false))
        assertEquals(FieldValidationMode.Blur, strategy.next(FieldValidationMode.Change, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Change, false))
        assertEquals(FieldValidationMode.Blur, strategy.next(FieldValidationMode.Submit, true))
        assertEquals(FieldValidationMode.Change, strategy.next(FieldValidationMode.Submit, false))
    }
}
