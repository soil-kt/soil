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
 * A type alias for validation rules that operate on any object type.
 *
 * Object rules are validation functions that take a value of any type and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 * This is the most generic form of validation rule.
 */
typealias ObjectRule<V> = ValidationRule<V>

/**
 * A type alias for builders that create object validation rules.
 *
 * Object rule builders provide a DSL for constructing validation rules
 * for any object type, with convenient methods like [satisfy] and [cast].
 */
typealias ObjectRuleBuilder<V> = ValidationRuleBuilder<V>

/**
 * A rule that tests the object value.
 *
 * @param predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [ObjectRule].
 */
fun <V> ObjectRule(
    predicate: V.() -> Boolean,
    message: () -> String
): ObjectRule<V> = ValidationRule { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the object value is equal to the expected value.
 *
 * This function creates a validation rule that checks if the current object value
 * equals the value returned by the [expected] function using the `==` operator.
 * The comparison is performed each time the validation runs, allowing for dynamic
 * expected values.
 *
 * Usage:
 * ```kotlin
 * // Password confirmation validation
 * rules<String> {
 *     equalTo({ passwordField.value }) { "Password confirmation must match the password" }
 * }
 * ```
 *
 * @param V The type of the object value being validated. Must be a non-null type.
 * @param expected A function that returns the expected value to compare against.
 *                 This is called each time validation runs, enabling dynamic comparisons.
 * @param message A function that returns the error message when validation fails.
 *                This is only called when the validation actually fails.
 */
fun <V : Any> ObjectRuleBuilder<V>.equalTo(expected: () -> V, message: () -> String) {
    extend(ObjectRule({ this == expected() }, message))
}

/**
 * Validates that the object value passes the given [predicate].
 *
 * Usage:
 * ```kotlin
 * rules<Post> {
 *     satisfy({ title.isNotBlank() }) { "Title must be not blank" }
 * }
 * ```
 *
 * @param predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 */
fun <V : Any> ObjectRuleBuilder<V>.satisfy(predicate: V.() -> Boolean, message: () -> String) {
    extend(ObjectRule(predicate, message))
}

/**
 * Creates a transformation-based validation rule chain.
 *
 * This function allows you to validate a transformed or derived value from the original object.
 * It's useful when you need to validate a computed property, extracted field, or any
 * transformation of the original value. The transformation is applied first, then the
 * chained validation rules are applied to the transformed result.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     notBlank { "must be not blank" }
 *     cast { it.length } then {
 *         minimum(3) { "must be at least 3 characters" }
 *         maximum(20) { "must be at most 20 characters" }
 *     }
 * }
 * ```
 *
 * @param V The type of the original object value.
 * @param S The type of the transformed value.
 * @param transform The transformation function to apply to the object value.
 * @return An [ValidationRuleChainer] that allows chaining validation rules for the transformed value.
 */
fun <V : Any, S> ObjectRuleBuilder<V>.cast(transform: (V) -> S): ValidationRuleChainer<S> =
    ValidationRuleChainer { block ->
        val ruleSet = rules(block)
        val chainedRule: ObjectRule<V> = ObjectRule { value ->
            validate(transform(value), ruleSet)
        }
        extend(chainedRule)
    }
