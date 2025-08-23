// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

/**
 * A type alias for validation rules that operate on CharSequence values.
 *
 * CharSequence rules are validation functions that take a CharSequence value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias CharSequenceRule = ValidationRule<CharSequence>

/**
 * A type alias for builders that create CharSequence validation rules.
 *
 * CharSequence rule builders provide a DSL for constructing validation rules
 * specifically for CharSequence values, with convenient methods like [notEmpty],
 * [notBlank], [minLength], [maxLength], and [match].
 */
typealias CharSequenceRuleBuilder = ValidationRuleBuilder<CharSequence>

/**
 * A rule that tests the CharSequence value.
 *
 * @param predicate The predicate to test the CharSequence value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [CharSequenceRule].
 */
fun CharSequenceRule(
    predicate: CharSequence.() -> Boolean,
    message: () -> String
): CharSequenceRule = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the CharSequence value is not empty.
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     notEmpty { "must be not empty" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.notEmpty(message: () -> String) {
    extend(CharSequenceRule(CharSequence::isNotEmpty, message))
}

/**
 * Validates that the CharSequence value is not blank.
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     notBlank { "must be not blank" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.notBlank(message: () -> String) {
    extend(CharSequenceRule(CharSequence::isNotBlank, message))
}

/**
 * Validates that the CharSequence length is at least [limit] characters.
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     minLength(3) { "must be at least 3 characters" }
 * }
 * ```
 *
 * @param limit The minimum number of characters the CharSequence must have.
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.minLength(limit: Int, message: () -> String) {
    extend(CharSequenceRule({ length >= limit }, message))
}

/**
 * Validates that the CharSequence length is no more than [limit] characters.
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     maxLength(20) { "must be at no more 20 characters" }
 * }
 * ```
 *
 * @param limit The maximum number of characters the CharSequence can have.
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.maxLength(limit: Int, message: () -> String) {
    extend(CharSequenceRule({ length <= limit }, message))
}

/**
 * Validates that the CharSequence matches the [pattern].
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     match("^[A-Za-z]+$") { "must be alphabetic" }
 * }
 * ```
 *
 * @param pattern The regular expression pattern the CharSequence must match.
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.match(pattern: String, message: () -> String) {
    match(Regex(pattern), message)
}

/**
 * Validates that the CharSequence matches the [pattern].
 *
 * Usage:
 * ```kotlin
 * rules<CharSequence> {
 *     match(Regex("^[A-Za-z]+$")) { "must be alphabetic" }
 * }
 * ```
 *
 * @param pattern The regular expression pattern the CharSequence must match.
 * @param message The message to return when the test fails.
 */
fun CharSequenceRuleBuilder.match(pattern: Regex, message: () -> String) {
    extend(CharSequenceRule({ pattern.matches(this) }, message))
}
