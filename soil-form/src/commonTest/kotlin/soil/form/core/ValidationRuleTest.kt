// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationRuleTest : UnitTest() {

    @Test
    fun validate_withEmptyRuleSet() {
        val rules: ValidationRuleSet<String> = emptySet()
        val actual = validate("test", rules)

        assertEquals(ValidationResult.Valid, actual)
    }

    @Test
    fun validate_withAllValidRules() {
        val rules: ValidationRuleSet<String> = setOf(
            { ValidationResult.Valid },
            { ValidationResult.Valid },
            { ValidationResult.Valid }
        )
        val actual = validate("test", rules)

        assertEquals(ValidationResult.Valid, actual)
    }

    @Test
    fun validate_withSingleInvalidRule() {
        val rules: ValidationRuleSet<String> = setOf(
            { ValidationResult.Invalid("Value is invalid") }
        )
        val actual = validate("test", rules)

        assertTrue(actual is ValidationResult.Invalid)
        assertEquals(1, actual.messages.size)
        assertEquals("Value is invalid", actual.messages[0])
    }

    @Test
    fun validate_withMultipleInvalidRules() {
        val rules: ValidationRuleSet<String> = setOf(
            { ValidationResult.Invalid("Error 1") },
            { ValidationResult.Invalid("Error 2") },
            { ValidationResult.Invalid("Error 3") }
        )
        val actual = validate("test", rules)

        assertTrue(actual is ValidationResult.Invalid)
        assertEquals(3, actual.messages.size)
        assertTrue(actual.messages.contains("Error 1"))
        assertTrue(actual.messages.contains("Error 2"))
        assertTrue(actual.messages.contains("Error 3"))
    }

    @Test
    fun validate_withMixedValidAndInvalidRules() {
        val rules: ValidationRuleSet<String> = setOf(
            { ValidationResult.Valid },
            { ValidationResult.Invalid("Error 1") },
            { ValidationResult.Valid },
            { ValidationResult.Invalid("Error 2") }
        )
        val result = validate("test", rules)

        assertTrue(result is ValidationResult.Invalid)
        assertEquals(2, result.messages.size)
        assertTrue(result.messages.contains("Error 1"))
        assertTrue(result.messages.contains("Error 2"))
    }
}
