// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules
import soil.form.core.validate

/**
 * A type alias for validation rules that operate on Array values.
 *
 * Array rules are validation functions that take an Array value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias ArrayRule<V> = ValidationRule<Array<V>>

/**
 * A type alias for builders that create Array validation rules.
 *
 * Array rule builders provide a DSL for constructing validation rules
 * specifically for Array values, with convenient methods like [notEmpty],
 * [minSize], and [maxSize].
 */
typealias ArrayRuleBuilder<V> = ValidationRuleBuilder<Array<V>>

/**
 * A rule that tests the array value.
 *
 * @param predicate The predicate to test the array value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [ArrayRule].
 */
fun <V> ArrayRule(
    predicate: Array<V>.() -> Boolean,
    message: () -> String
): ArrayRule<V> = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
class ArrayRuleTester<V>(
    predicate: Array<V>.() -> Boolean,
    message: () -> String
) : ArrayRule<V> by ArrayRule(predicate, message)

/**
 * Validates that the array is not empty.
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     notEmpty { "must be not empty" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.notEmpty(message: () -> String) {
    extend(ArrayRule(Array<V>::isNotEmpty, message))
}

/**
 * Validates that the array size is at least [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     minSize(3) { "must have at least 3 items" }
 * }
 * ```
 *
 * @param limit The minimum number of elements the array must have.
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.minSize(limit: Int, message: () -> String) {
    extend(ArrayRule({ size >= limit }, message))
}

/**
 * Validates that the array size is no more than [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     maxSize(20) { "must have at no more 20 items" }
 * }
 * ```
 *
 * @param limit The maximum number of elements the array can have.
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.maxSize(limit: Int, message: () -> String) {
    extend(ArrayRule({ size <= limit }, message))
}

/**
 * Creates a validation rule chain for applying rules to each element of the array.
 *
 * This function allows you to validate each individual element within an array
 * using the `all` function internally. It's useful when you need to ensure that
 * every element in the array meets certain criteria.
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     notEmpty { "array must not be empty" }
 *     element {
 *         notBlank { "must be not blank" }
 *         minLength(3) { "must be at least 3 characters" }
 *     }
 * }
 * ```
 *
 * @param V The type of the elements in the array.
 * @param block A lambda that builds the validation rules using [ValidationRuleBuilder].
 */
fun <V> ArrayRuleBuilder<V>.element(block: ValidationRuleBuilder<V>.() -> Unit) {
    val ruleSet = rules(block)
    val chainedRule: ArrayRule<V> = { collection ->
        val allErrorMessages = collection.flatMap { element ->
            when (val result = validate(element, ruleSet)) {
                is ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.messages
            }
        }
        if (allErrorMessages.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(allErrorMessages.distinct())
        }
    }
    extend(chainedRule)
}
