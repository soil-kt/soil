// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BooleanRuleTest : UnitTest() {

    @Test
    fun rule_truthy() {
        val rule = testRule {
            truthy { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(true))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(false))
    }

    @Test
    fun rule_falsy() {
        val rule = testRule {
            falsy { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(false))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(true))
    }

    @Test
    fun rule_custom() {
        val custom = BooleanRule({ this }) { "Must be true!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(true))
        assertEquals(ValidationResult.Invalid("Must be true!"), rule(false))
    }

    private fun testRule(block: BooleanRuleBuilder.() -> Unit): BooleanRule = createTestRule(block)
}
