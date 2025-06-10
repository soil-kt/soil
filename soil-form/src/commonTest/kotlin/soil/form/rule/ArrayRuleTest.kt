// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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

    @Test
    fun rule_complex_validation() {
        val rule = testRule {
            minSize(2) { "min" }
            maxSize(4) { "max" }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(arrayOf("a", "b", "c", "d")))
        assertEquals(ValidationResult.Invalid("min"), rule(arrayOf("a")))
        assertEquals(ValidationResult.Invalid("max"), rule(arrayOf("a", "b", "c", "d", "e")))
    }

    @Test
    fun rule_element_validation() {
        val rule = testRule {
            element {
                notBlank { "must be not blank" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyArray<String>()))
        assertEquals(
            ValidationResult.Invalid("Element at index 1: must be not blank"),
            rule(arrayOf("hello", ""))
        )
        assertEquals(
            ValidationResult.Invalid("Element at index 0: must be not blank"),
            rule(arrayOf("", "world"))
        )
    }

    @Test
    fun rule_element_multiple_validations() {
        val rule = testRule {
            element {
                notBlank { "must be not blank" }
                minLength(3) { "must be at least 3 characters" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyArray<String>()))
        
        val result = rule(arrayOf("hi", ""))
        assertEquals(ValidationResult.Invalid::class, result::class)
        val invalidResult = result as ValidationResult.Invalid
        assertEquals(2, invalidResult.messages.size)
        assertEquals("Element at index 0: must be at least 3 characters", invalidResult.messages[0])
        assertEquals("Element at index 1: must be not blank", invalidResult.messages[1])
    }

    @Test
    fun rule_combined_array_and_element_validation() {
        val rule = testRule {
            minSize(1) { "array must not be empty" }
            element {
                notBlank { "must be not blank" }
                minLength(2) { "must be at least 2 characters" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(arrayOf("hello", "world")))
        assertEquals(ValidationResult.Invalid("array must not be empty"), rule(emptyArray<String>()))
        
        val result = rule(arrayOf("a", ""))
        assertEquals(ValidationResult.Invalid::class, result::class)
        val invalidResult = result as ValidationResult.Invalid
        assertEquals(2, invalidResult.messages.size)
        assertEquals("Element at index 0: must be at least 2 characters", invalidResult.messages[0])
        assertEquals("Element at index 1: must be not blank", invalidResult.messages[1])
    }

    private fun testRule(block: ArrayRuleBuilder<String>.() -> Unit): ArrayRule<String> = createTestRule(block)
}
