// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionRuleTest : UnitTest() {

    @Test
    fun rule_notEmpty() {
        val rule = testRule {
            notEmpty { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(setOf("a", "b", "c")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyList()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptySet()))
    }

    @Test
    fun rule_minSize() {
        val rule = testRule {
            minSize(2) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b", "c")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("a")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyList()))
    }

    @Test
    fun rule_maxSize() {
        val rule = testRule {
            maxSize(3) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b", "c")))
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(emptyList()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("a", "b", "c", "d")))
    }

    @Test
    fun rule_element() {
        val rule = testRule {
            element {
                notBlank { "Invalid!" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyList()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("hello", "")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("", "world")))
    }

    @Test
    fun rule_custom() {
        val custom = CollectionRule<String>({ all { it.isNotBlank() } }) { "All elements must be non-blank!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(listOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyList()))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(listOf("hello", "")))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(listOf("", "world")))
    }

    @Test
    fun rule_complex_validation() {
        val rule = testRule {
            minSize(2) { "min" }
            maxSize(5) { "max" }
            element {
                notBlank { "blank" }
            }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b", "c", "d", "e")))
        assertEquals(ValidationResult.Invalid("min"), rule(listOf("a")))
        assertEquals(ValidationResult.Invalid("max"), rule(listOf("a", "b", "c", "d", "e", "f")))
        assertEquals(ValidationResult.Invalid("blank"), rule(listOf("a", "b", "", "   ")))
    }

    private fun testRule(block: CollectionRuleBuilder<String>.() -> Unit): CollectionRule<String> =
        createTestRule(block)
}
