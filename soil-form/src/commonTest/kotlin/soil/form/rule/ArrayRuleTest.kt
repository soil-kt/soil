// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArrayRuleTest : UnitTest() {

    @Test
    fun rule_notEmpty() {
        val rule = testRule {
            notEmpty { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyArray<String>()))
    }

    @Test
    fun rule_minSize() {
        val rule = testRule {
            minSize(2) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b", "c")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(arrayOf("a")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyArray<String>()))
    }

    @Test
    fun rule_maxSize() {
        val rule = testRule {
            maxSize(3) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b", "c")))
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(emptyArray<String>()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(arrayOf("a", "b", "c", "d")))
    }

    @Test
    fun rule_custom() {
        val custom = ArrayRule<String>({ all { it.isNotBlank() } }) { "All elements must be non-blank!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyArray<String>()))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(arrayOf("hello", "")))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(arrayOf("", "world")))
    }

    private fun testRule(block: ArrayRuleBuilder<String>.() -> Unit): ArrayRule<String> {
        return rules(block).first()
    }
}
