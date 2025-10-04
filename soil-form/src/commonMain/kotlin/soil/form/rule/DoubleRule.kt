// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

/**
 * A type alias for validation rules that operate on Double values.
 *
 * Double rules are validation functions that take a Double value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias DoubleRule = ValidationRule<Double>

/**
 * A type alias for builders that create Double validation rules.
 *
 * Double rule builders provide a DSL for constructing validation rules
 * specifically for Double values, with convenient methods like [minimum],
 * [maximum], and [notNaN].
 */
typealias DoubleRuleBuilder = ValidationRuleBuilder<Double>

/**
 * A rule that tests the double value.
 *
 * @param predicate The predicate to test the double value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [DoubleRule].
 */
fun DoubleRule(
    predicate: Double.() -> Boolean,
    message: () -> String
): DoubleRule = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the double value is greater than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Double> {
 *     minimum(3.0) { "must be greater than or equal to 3.0" }
 * }
 * ```
 *
 * @param limit The minimum value the double must have.
 * @param message The message to return when the test fails.
 */
fun DoubleRuleBuilder.minimum(limit: Double, message: () -> String) {
    extend(DoubleRule({ this >= limit }, message))
}

/**
 * Validates that the double value is less than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Double> {
 *     maximum(20.0) { "must be less than or equal to 20.0" }
 * }
 * ```
 *
 * @param limit The maximum value the double can have.
 * @param message The message to return when the test fails.
 */
fun DoubleRuleBuilder.maximum(limit: Double, message: () -> String) {
    extend(DoubleRule({ this <= limit }, message))
}

/**
 * Validates that the double value is not `NaN`.
 *
 * Usage:
 * ```kotlin
 * rules<Double> {
 *     notNaN { "must be not NaN" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun DoubleRuleBuilder.notNaN(message: () -> String) {
    extend(DoubleRule({ !isNaN() }, message))
}
