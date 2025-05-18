// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

typealias StringRule = ValidationRule<String>
typealias StringRuleBuilder = ValidationRuleBuilder<String>

/**
 * A rule that tests the string value.
 *
 * @param predicate The predicate to test the string value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [StringRule].
 */
fun StringRule(
    predicate: String.() -> Boolean,
    message: () -> String
): StringRule = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

/**
 * Validates that the string value is not empty.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     notEmpty { "must be not empty" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.notEmpty(message: () -> String) {
    extend(StringRule(String::isNotEmpty, message))
}

/**
 * Validates that the string value is not blank.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     notBlank { "must be not blank" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.notBlank(message: () -> String) {
    extend(StringRule(String::isNotBlank, message))
}

/**
 * Validates that the string length is at least [limit] characters.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     minLength(3) { "must be at least 3 characters" }
 * }
 * ```
 *
 * @param limit The minimum number of characters the string must have.
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.minLength(limit: Int, message: () -> String) {
    extend(StringRule({ length >= limit }, message))
}

/**
 * Validates that the string length is no more than [limit] characters.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     maxLength(20) { "must be at no more 20 characters" }
 * }
 * ```
 *
 * @param limit The maximum number of characters the string can have.
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.maxLength(limit: Int, message: () -> String) {
    extend(StringRule({ length <= limit }, message))
}

/**
 * Validates that the string matches the [pattern].
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     pattern("^[A-Za-z]+$") { "must be alphabetic" }
 * }
 * ```
 *
 * @param pattern The regular expression pattern the string must match.
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.pattern(pattern: String, message: () -> String) {
    pattern(Regex(pattern), message)
}

/**
 * Validates that the string matches the [pattern].
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     pattern(Regex("^[A-Za-z]+$")) { "must be alphabetic" }
 * }
 * ```
 *
 * @param pattern The regular expression pattern the string must match.
 * @param message The message to return when the test fails.
 */
fun StringRuleBuilder.pattern(pattern: Regex, message: () -> String) {
    extend(StringRule({ pattern.matches(this) }, message))
}
