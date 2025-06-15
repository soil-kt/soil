// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DoubleRuleTest : UnitTest() {

    @Test
    fun rule_minimum() {
        val rule = testRule {
            minimum(5.0) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5.0))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(4.9))
    }

    @Test
    fun rule_maximum() {
        val rule = testRule {
            maximum(10.0) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(10.0))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(10.1))
    }

    @Test
    fun rule_notNaN() {
        val rule = testRule {
            notNaN { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5.0))
        assertEquals(ValidationResult.Valid, rule(0.0))
        assertEquals(ValidationResult.Valid, rule(-5.0))
        assertEquals(ValidationResult.Valid, rule(Double.POSITIVE_INFINITY))
        assertEquals(ValidationResult.Valid, rule(Double.NEGATIVE_INFINITY))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Double.NaN))
    }

    @Test
    fun rule_custom() {
        val custom = DoubleRule({ this > 0.0 && this < 1.0 }) { "Must be between 0 and 1!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(0.5))
        assertEquals(ValidationResult.Valid, rule(0.1))
        assertEquals(ValidationResult.Valid, rule(0.9))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(0.0))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(1.0))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(-0.5))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(1.5))
    }

    @Test
    fun rule_complex_validation() {
        val rule = testRule {
            minimum(1.0) { "min" }
            maximum(10.0) { "max" }
        }
        assertEquals(ValidationResult.Valid, rule(1.0))
        assertEquals(ValidationResult.Valid, rule(5.5))
        assertEquals(ValidationResult.Valid, rule(10.0))
        assertEquals(ValidationResult.Invalid("min"), rule(0.9))
        assertEquals(ValidationResult.Invalid("max"), rule(10.1))
    }

    private fun testRule(block: DoubleRuleBuilder.() -> Unit): DoubleRule = createTestRule(block)
}
