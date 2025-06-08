// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StringRuleTest : UnitTest() {

    @Test
    fun rule_notEmpty() {
        val rule = testRule {
            notEmpty { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Valid, rule(" "))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(""))
    }

    @Test
    fun rule_notBlank() {
        val rule = testRule {
            notBlank { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(" "))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(""))
    }


    @Test
    fun rule_minLength() {
        val rule = testRule {
            minLength(3) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule("foo"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule("fo"))
    }


    @Test
    fun rule_maxLength() {
        val rule = testRule {
            maxLength(10) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule("helloworld"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule("hello world"))
    }

    @Test
    fun rule_match() {
        val rule = testRule {
            match("^[a-zA-Z]+$") { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule("hello"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule("hello123"))
    }

    @Test
    fun rule_custom() {
        val custom = StringRule({ startsWith("test") }) { "Invalid!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule("test123"))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule("hello"))
    }

    private fun testRule(block: StringRuleBuilder.() -> Unit): StringRule {
        return rules(block).first()
    }
}
