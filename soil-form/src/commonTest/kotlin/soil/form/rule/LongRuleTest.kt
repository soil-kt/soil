// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LongRuleTest : UnitTest() {

    @Test
    fun rule_minimum() {
        val rule = testRule {
            minimum(5L) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(5L))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(4L))
    }

    @Test
    fun rule_maximum() {
        val rule = testRule {
            maximum(10L) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(10L))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(11L))
    }

    @Test
    fun rule_custom() {
        val custom = LongRule({ this % 2L == 0L }) { "Must be even!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(2L))
        assertEquals(ValidationResult.Valid, rule(0L))
        assertEquals(ValidationResult.Valid, rule(-4L))
        assertEquals(ValidationResult.Invalid("Must be even!"), rule(1L))
        assertEquals(ValidationResult.Invalid("Must be even!"), rule(3L))
        assertEquals(ValidationResult.Invalid("Must be even!"), rule(-3L))
    }

    private fun testRule(block: LongRuleBuilder.() -> Unit): LongRule {
        return rules(block).first()
    }
}
