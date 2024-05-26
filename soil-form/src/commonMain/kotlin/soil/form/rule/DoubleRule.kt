// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias DoubleRule = ValidationRule<Double>
typealias DoubleRuleBuilder = ValidationRuleBuilder<Double>

/**
 * A rule that tests the double value.
 *
 * @property predicate The predicate to test the double value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [DoubleRuleTester].
 */
class DoubleRuleTester(
    val predicate: Double.() -> Boolean,
    val message: () -> String
) : DoubleRule {
    override fun test(value: Double): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
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
    extend(DoubleRuleTester({ this >= limit }, message))
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
    extend(DoubleRuleTester({ this <= limit }, message))
}

/**
 * Validates that the double value is `NaN`.
 *
 * Usage:
 * ```kotlin
 * rules<Double> {
 *     isNaN { "must be NaN" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun DoubleRuleBuilder.isNaN(message: () -> String) {
    extend(DoubleRuleTester({ isNaN() }, message))
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
    extend(DoubleRuleTester({ !isNaN() }, message))
}
