// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
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
    fun rule_notNull_without_then() {
        val rule = testRule {
            notNull { "Value is required!" }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Valid, rule(""))
        assertEquals(ValidationResult.Invalid("Value is required!"), rule(null))
    }

    @Test
    fun rule_notNull_with_multiple_rules() {
        val rule = testRule {
            notNull { "Required!" }
            // Add another rule that should also be applied
            extend { value: String? ->
                if (value != null && value.length < 3) {
                    ValidationResult.Invalid("Too short!")
                } else {
                    ValidationResult.Valid
                }
            }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Invalid("Required!"), rule(null))
        assertEquals(ValidationResult.Invalid("Too short!"), rule("ab"))
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

    @Test
    fun rule_complex_validation() {
        val rule = testRule {
            notNull { "null" } then {
                minLength(3) { "min" }
                maxLength(10) { "max" }
            }
        }
        assertEquals(ValidationResult.Valid, rule("test"))
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Valid, rule("1234567890"))
        assertEquals(ValidationResult.Invalid("null"), rule(null))
        assertEquals(ValidationResult.Invalid("min"), rule("ab"))
        assertEquals(ValidationResult.Invalid("max"), rule("12345678901"))
    }

    private fun testRule(block: OptionalRuleBuilder<String?>.() -> Unit): OptionalRule<String> = createTestRule(block)
}
