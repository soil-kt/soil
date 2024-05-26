// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias StringRule = ValidationRule<String>
typealias StringRuleBuilder = ValidationRuleBuilder<String>

/**
 * A rule that tests the string value.
 *
 * @property predicate The predicate to test the string value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [StringRuleTester].
 */
class StringRuleTester(
    val predicate: String.() -> Boolean,
    val message: () -> String
) : StringRule {
    override fun test(value: String): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
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
    extend(StringRuleTester(String::isNotEmpty, message))
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
    extend(StringRuleTester(String::isNotBlank, message))
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
    extend(StringRuleTester({ length >= limit }, message))
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
    extend(StringRuleTester({ length <= limit }, message))
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
    extend(StringRuleTester({ pattern.matches(this) }, message))
}
