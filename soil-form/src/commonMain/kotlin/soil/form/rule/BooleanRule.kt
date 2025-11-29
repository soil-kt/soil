// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

/**
 * A type alias for validation rules that operate on Boolean values.
 *
 * Boolean rules are validation functions that take a Boolean value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias BooleanRule = ValidationRule<Boolean>

/**
 * A type alias for builders that create Boolean validation rules.
 *
 * Boolean rule builders provide a DSL for constructing validation rules
 * specifically for Boolean values, with convenient methods like [truthy] and [falsy].
 */
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
): BooleanRule = ValidationRule { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the boolean value is `true`.
 *
 * Usage:
 * ```kotlin
 * rules<Boolean> {
 *     truthy { "must be true" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun BooleanRuleBuilder.truthy(message: () -> String) {
    extend(BooleanRule({ this }, message))
}

/**
 * Validates that the boolean value is `false`.
 *
 * Usage:
 * ```kotlin
 * rules<Boolean> {
 *     falsy { "must be false" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun BooleanRuleBuilder.falsy(message: () -> String) {
    extend(BooleanRule({ !this }, message))
}
