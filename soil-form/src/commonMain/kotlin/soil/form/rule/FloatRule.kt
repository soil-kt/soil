// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias FloatRule = ValidationRule<Float>
typealias FloatRuleBuilder = ValidationRuleBuilder<Float>

/**
 * A rule that tests the float value.
 *
 * @property predicate The predicate to test the float value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [FloatRuleTester].
 */
class FloatRuleTester(
    val predicate: Float.() -> Boolean,
    val message: () -> String
) : FloatRule {
    override fun test(value: Float): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * Validates that the float value is greater than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Float> {
 *     minimum(3.0f) { "must be greater than or equal to 3.0" }
 * }
 * ```
 *
 * @param limit The minimum value the float must have.
 * @param message The message to return when the test fails.
 */
fun FloatRuleBuilder.minimum(limit: Float, message: () -> String) {
    extend(FloatRuleTester({ this >= limit }, message))
}

/**
 * Validates that the float value is less than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Float> {
 *     maximum(20.0f) { "must be less than or equal to 20.0" }
 * }
 * ```
 *
 * @param limit The maximum value the float can have.
 * @param message The message to return when the test fails.
 */
fun FloatRuleBuilder.maximum(limit: Float, message: () -> String) {
    extend(FloatRuleTester({ this <= limit }, message))
}

/**
 * Validates that the float value is `NaN`.
 *
 * Usage:
 * ```kotlin
 * rules<Float> {
 *     isNaN { "must be NaN" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun FloatRuleBuilder.isNaN(message: () -> String) {
    extend(FloatRuleTester({ isNaN() }, message))
}

/**
 * Validates that the float value is not `NaN`.
 *
 * Usage:
 * ```kotlin
 * rules<Float> {
 *     notNaN { "must be not NaN" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun FloatRuleBuilder.notNaN(message: () -> String) {
    extend(FloatRuleTester({ !isNaN() }, message))
}
