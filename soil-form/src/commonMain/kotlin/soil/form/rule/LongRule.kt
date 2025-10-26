// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

/**
 * A type alias for validation rules that operate on Long values.
 *
 * Long rules are validation functions that take a Long value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias LongRule = ValidationRule<Long>

/**
 * A type alias for builders that create Long validation rules.
 *
 * Long rule builders provide a DSL for constructing validation rules
 * specifically for Long values, with convenient methods like [minimum] and [maximum].
 */
typealias LongRuleBuilder = ValidationRuleBuilder<Long>

/**
 * A rule that tests the long value.
 *
 * @param predicate The predicate to test the long value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [LongRule].
 */
fun LongRule(
    predicate: Long.() -> Boolean,
    message: () -> String
): LongRule = ValidationRule { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
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
    extend(LongRule({ this >= limit }, message))
}

/**
 * Validates that the long value is less than or equal to [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Long> {
 *     maximum(20) { "must be less than or equal to 20" }
 * }
 * ```
 *
 * @param limit The maximum value the long can have.
 * @param message The message to return when the test fails.
 */
fun LongRuleBuilder.maximum(limit: Long, message: () -> String) {
    extend(LongRule({ this <= limit }, message))
}
