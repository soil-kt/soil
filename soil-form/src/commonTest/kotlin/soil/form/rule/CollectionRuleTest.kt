// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.rules
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
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyList<String>()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptySet<String>()))
    }

    @Test
    fun rule_minSize() {
        val rule = testRule {
            minSize(2) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b", "c")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("a")))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(emptyList<String>()))
    }

    @Test
    fun rule_maxSize() {
        val rule = testRule {
            maxSize(3) { "Invalid!" }
        }
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b", "c")))
        assertEquals(ValidationResult.Valid, rule(listOf("a", "b")))
        assertEquals(ValidationResult.Valid, rule(emptyList<String>()))
        assertEquals(ValidationResult.Invalid("Invalid!"), rule(listOf("a", "b", "c", "d")))
    }

    @Test
    fun rule_custom() {
        val custom = CollectionRule<String>({ all { it.isNotBlank() } }) { "All elements must be non-blank!" }
        val rule = testRule {
            extend(custom)
        }
        assertEquals(ValidationResult.Valid, rule(listOf("hello", "world")))
        assertEquals(ValidationResult.Valid, rule(emptyList<String>()))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(listOf("hello", "")))
        assertEquals(ValidationResult.Invalid("All elements must be non-blank!"), rule(listOf("", "world")))
    }

    private fun testRule(block: CollectionRuleBuilder<String>.() -> Unit): CollectionRule<String> {
        return rules(block).first()
    }
}
