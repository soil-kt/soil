// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias LongRule = ValidationRule<Long>
typealias LongRuleBuilder = ValidationRuleBuilder<Long>

/**
 * A rule that tests the long value.
 *
 * @property predicate The predicate to test the long value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [LongRuleTester].
 */
class LongRuleTester(
    val predicate: Long.() -> Boolean,
    val message: () -> String
) : LongRule {
    override fun test(value: Long): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * Validates that the long value is greater than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Long> {
 *     minimum(3) { "must be greater than or equal to 3" }
 * }
 * ```
 *
 * @param limit The minimum value the long must have.
 * @param message The message to return when the test fails.
 */
fun LongRuleBuilder.minimum(limit: Long, message: () -> String) {
    extend(LongRuleTester({ this >= limit }, message))
}

/**
 * Validates that the long value is less than or equal to [limit].
 *
 * rules<Long> {
 *     maximum(20) { "must be less than or equal to 20" }
 * }
 *
 * @param limit The maximum value the long can have.
 * @param message The message to return when the test fails.
 */
fun LongRuleBuilder.maximum(limit: Long, message: () -> String) {
    extend(LongRuleTester({ this <= limit }, message))
}
