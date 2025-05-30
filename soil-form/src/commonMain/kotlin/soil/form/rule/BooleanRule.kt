// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

typealias BooleanRule = ValidationRule<Boolean>
typealias BooleanRuleBuilder = ValidationRuleBuilder<Boolean>

/**
 * A rule that tests the boolean value.
 *
 * @param predicate The predicate to test the boolean value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [BooleanRule].
 */
fun BooleanRule(
    predicate: Boolean.() -> Boolean,
    message: () -> String
): BooleanRule = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

@Deprecated("Legacy")
class BooleanRuleTester(
    predicate: Boolean.() -> Boolean,
    message: () -> String
) : BooleanRule by BooleanRule(predicate, message)

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
    extend(BooleanRule({ this }, message))
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
    extend(BooleanRule({ !this }, message))
}
