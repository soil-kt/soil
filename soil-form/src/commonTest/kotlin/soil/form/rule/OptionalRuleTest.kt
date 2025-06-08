// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OptionalRuleTest : UnitTest() {

    @Test
    fun rule_notNull() {
        val rule = testRule {
            notNull { "Invalid!" } then {
                // Add a simple rule to avoid empty rule set
                notEmpty { "Must not be empty!" }
            }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(null))
    }

    @Test
    fun rule_custom_optional() {
        val customRule: OptionalRule<String> = { value ->
            when {
                value == null -> ValidationResult.Invalid("Custom: Value is required!")
                value.startsWith("test") -> ValidationResult.Valid
                else -> ValidationResult.Invalid("Custom: Must start with 'test'!")
            }
        }
        val rule = testRule {
            extend(customRule)
        }
        assertEquals(ValidationResult.Valid, rule("test123"))
        assertEquals(ValidationResult.Valid, rule("test"))
        assertEquals(ValidationResult.Invalid("Custom: Value is required!"), rule(null))
        assertEquals(ValidationResult.Invalid("Custom: Must start with 'test'!"), rule("hello"))
        assertEquals(ValidationResult.Invalid("Custom: Must start with 'test'!"), rule(""))
    }

    private fun testRule(block: OptionalRuleBuilder<String?>.() -> Unit): OptionalRule<String> {
        return rules(block).first()
    }
}
