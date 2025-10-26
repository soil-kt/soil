// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

/**
 * A type alias for validation rules that operate on Int values.
 *
 * Int rules are validation functions that take an Int value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias IntRule = ValidationRule<Int>

/**
 * A type alias for builders that create Int validation rules.
 *
 * Int rule builders provide a DSL for constructing validation rules
 * specifically for Int values, with convenient methods like [minimum] and [maximum].
 */
typealias IntRuleBuilder = ValidationRuleBuilder<Int>

/**
 * A rule that tests the integer value.
 *
 * @param predicate The predicate to test the integer value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [IntRule].
 */
fun IntRule(
    predicate: Int.() -> Boolean,
    message: () -> String
): IntRule = ValidationRule { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the integer value is greater than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Int> {
 *     minimum(3) { "must be greater than or equal to 3" }
 * }
 * ```
 *
 * @param limit The minimum value the integer must have.
 * @param message The message to return when the test fails.
 */
fun IntRuleBuilder.minimum(limit: Int, message: () -> String) {
    extend(IntRule({ this >= limit }, message))
}

/**
 * Validates that the integer value is less than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Int> {
 *     maximum(20) { "must be less than or equal to 20" }
 * }
 * ```
 *
 * @param limit The maximum value the integer can have.
 * @param message The message to return when the test fails.
 */
fun IntRuleBuilder.maximum(limit: Int, message: () -> String) {
    extend(IntRule({ this <= limit }, message))
}
