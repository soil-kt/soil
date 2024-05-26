// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Represents a validation rule for a form field.
 *
 * @param V The type of the value to be validated.
 */
fun interface ValidationRule<V> {

    /**
     * Tests the given value against this rule.
     *
     * @param value The value to be tested.
     * @return The errors found in the value. If the value is valid, this should be an empty list.
     */
    fun test(value: V): FieldErrors
}

/**
 * A set of validation rules.
 *
 * @param V The type of the value to be validated.
 */
typealias ValidationRuleSet<V> = Set<ValidationRule<V>>
