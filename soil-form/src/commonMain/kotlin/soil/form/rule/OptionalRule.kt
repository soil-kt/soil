// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.ValidationRuleChainer
import soil.form.core.rules
import soil.form.core.validate

/**
 * A type alias for validation rules that operate on optional (nullable) values.
 *
 * Optional rules are validation functions that take a nullable value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias OptionalRule<V> = ValidationRule<V?>

/**
 * A type alias for builders that create optional validation rules.
 *
 * Optional rule builders provide a DSL for constructing validation rules
 * specifically for nullable values, with convenient methods like [notNull].
 */
typealias OptionalRuleBuilder<V> = ValidationRuleBuilder<V?>

/**
 * Validates that the optional value is not `null`.
 *
 * This function creates a validation rule that checks if a nullable value is not null.
 * It returns an [ValidationRuleChainer] that allows you to chain additional validation
 * rules using the `then` infix function. The chained rules will only be applied
 * if the value is not null.
 *
 * Usage:
 * ```kotlin
 * rules<String?> {
 *     notNull { "Value is required" } then {
 *         notBlank { "Value cannot be blank" }
 *         minLength(3) { "Value must be at least 3 characters" }
 *     }
 * }
 * ```
 *
 * @param V The non-nullable type of the value being validated.
 * @param message A function that returns the error message when the value is `null`.
 * @return An [ValidationRuleChainer] that allows chaining additional validation rules.
 */
fun <V : Any> OptionalRuleBuilder<V?>.notNull(message: () -> String): ValidationRuleChainer<V> =
    ValidationRuleChainer { block ->
        val ruleSet = rules(block)
        val chainedRule: OptionalRule<V> = { value ->
            if (value == null) {
                ValidationResult.Invalid(message())
            } else {
                validate(value, ruleSet)
            }
        }
        extend(chainedRule)
    }
