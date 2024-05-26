// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias IntRule = ValidationRule<Int>
typealias IntRuleBuilder = ValidationRuleBuilder<Int>

/**
 * A rule that tests the integer value.
 *
 * @property predicate The predicate to test the integer value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [IntRuleTester].
 */
class IntRuleTester(
    val predicate: Int.() -> Boolean,
    val message: () -> String
) : IntRule {
    override fun test(value: Int): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
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
    extend(IntRuleTester({ this >= limit }, message))
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
    extend(IntRuleTester({ this <= limit }, message))
}
