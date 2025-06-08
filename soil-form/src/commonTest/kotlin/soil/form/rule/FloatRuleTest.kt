// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FloatRuleTest : UnitTest() {

    @Test
    fun rule_minimum() {
        val rule = testRule {
            minimum(5.0f) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5.0f))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(4.9f))
    }

    @Test
    fun rule_maximum() {
        val rule = testRule {
            maximum(10.0f) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(10.0f))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(10.1f))
    }

    @Test
    fun rule_notNaN() {
        val rule = testRule {
            notNaN { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5.0f))
        assertEquals(ValidationResult.Valid, rule(0.0f))
        assertEquals(ValidationResult.Valid, rule(-5.0f))
        assertEquals(ValidationResult.Valid, rule(Float.POSITIVE_INFINITY))
        assertEquals(ValidationResult.Valid, rule(Float.NEGATIVE_INFINITY))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(Float.NaN))
    }

    @Test
    fun rule_custom() {
        val custom = FloatRule({ this > 0.0f && this < 1.0f }) { "Must be between 0 and 1!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(0.5f))
        assertEquals(ValidationResult.Valid, rule(0.1f))
        assertEquals(ValidationResult.Valid, rule(0.9f))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(0.0f))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(1.0f))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(-0.5f))
        assertEquals(ValidationResult.Invalid("Must be between 0 and 1!"), rule(1.5f))
    }

    private fun testRule(block: FloatRuleBuilder.() -> Unit): FloatRule {
        return rules(block).first()
    }
}
