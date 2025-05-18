// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

typealias ValidationRule<V> = (value: V) -> ValidationResult

/**
 * A set of validation rules.
 *
 * @param V The type of the value to be validated.
 */
typealias ValidationRuleSet<V> = Set<ValidationRule<V>>
