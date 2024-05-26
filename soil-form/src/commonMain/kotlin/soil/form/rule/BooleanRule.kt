// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias BooleanRule = ValidationRule<Boolean>
typealias BooleanRuleBuilder = ValidationRuleBuilder<Boolean>

/**
 * A rule that tests the boolean value.
 *
 * @property predicate The predicate to test the boolean value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [BooleanRuleTester].
 */
class BooleanRuleTester(
    val predicate: Boolean.() -> Boolean,
    val message: () -> String
) : BooleanRule {
    override fun test(value: Boolean): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * Validates that the boolean value is `true`.
 *
 * Usage:
 * ```kotlin
 * rules<Boolean> {
 *     isTrue { "must be true" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun BooleanRuleBuilder.isTrue(message: () -> String) {
    extend(BooleanRuleTester({ this }, message))
}

/**
 * Validates that the boolean value is `false`.
 *
 * Usage:
 * ```kotlin
 * rules<Boolean> {
 *     isFalse { "must be false" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun BooleanRuleBuilder.isFalse(message: () -> String) {
    extend(BooleanRuleTester({ !this }, message))
}
