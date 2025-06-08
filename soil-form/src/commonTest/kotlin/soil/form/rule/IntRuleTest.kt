// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IntRuleTest : UnitTest() {

    @Test
    fun rule_minimum() {
        val rule = testRule {
            minimum(5) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(4))
    }

    @Test
    fun rule_maximum() {
        val rule = testRule {
            maximum(10) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(10))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(11))
    }

    @Test
    fun rule_custom() {
        val custom = IntRule({ this % 2 == 0 }) { "Invalid!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(2))
        assertEquals(ValidationResult.Valid, rule(0))
        assertEquals(ValidationResult.Valid, rule(-4))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(1))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(3))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(-3))
    }

    private fun testRule(block: IntRuleBuilder.() -> Unit): IntRule {
        return rules(block).first()
    }
}
