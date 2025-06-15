// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

/**
 * A type alias for validation rule functions.
 *
 * A validation rule is a function that takes a value of type V and returns a [ValidationResult]
 * indicating whether the validation passed or failed. This is the fundamental building block
 * for all validation logic in the form system.
 *
 * The form system provides type-specific rule extensions in the `soil.form.rule` package
 * for common data types (String, Int, Double, Boolean, Array, Collection, etc.), which offer
 * convenient DSL methods for building validation rules. For custom validation logic, you can
 * create your own extension functions on the appropriate rule builder types.
 *
 * Using type-specific extensions:
 * ```kotlin
 * val stringRules = rules<String> {
 *     notEmpty { "Value cannot be empty" }
 *     minLength(3) { "Must be at least 3 characters" }
 * }
 * ```
 *
 * Creating custom validation extensions:
 * ```kotlin
 * fun StringRuleBuilder.email(message: () -> String) {
 *     val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
 *     extend(StringRule({ pattern.matches(this) }, message))
 * }
 *
 * val emailRules = rules<String> {
 *     notBlank { "Email is required" }
 *     email { "Must be a valid email address" }
 * }
 * ```
 *
 * @param V The type of the value to be validated.
 */
typealias ValidationRule<V> = (value: V) -> ValidationResult

/**
 * A type alias for a set of validation rules.
 *
 * A validation rule set represents a collection of validation rules that can be applied
 * to a value. All rules in the set are typically evaluated, and any failures are collected
 * into a combined validation result.
 *
 * Usage:
 * ```kotlin
 * val stringRules: ValidationRuleSet<String> = setOf(
 *     { value -> if (value.isNotBlank()) ValidationResult.Valid else ValidationResult.Invalid("Required") },
 *     { value -> if (value.length >= 3) ValidationResult.Valid else ValidationResult.Invalid("Too short") }
 * )
 * ```
 *
 * @param V The type of the value to be validated.
 */
typealias ValidationRuleSet<V> = Set<ValidationRule<V>>

/**
 * Validates a value against a set of validation rules.
 *
 * This function applies all the provided validation rules to the given value
 * and collects any validation error messages. If all rules pass, it returns
 * [ValidationResult.Valid]. If any rules fail, it returns a [ValidationResult.Invalid] containing
 * all the error messages.
 *
 * Usage:
 * ```kotlin
 * val rules = rules<String> {
 *     notBlank { "Required" }
 *     minLength(3) { "Too short" }
 * }
 * val result = validate("ab", rules)
 * // ValidationResult.Invalid.messages will contain ["Too short"]
 * ```
 *
 * @param V The type of the value being validated.
 * @param value The value to validate.
 * @param rules The set of validation rules to apply.
 * @return A [ValidationResult.Invalid] containing any validation error messages, or [ValidationResult.Valid] if validation passes.
 */
fun <V> validate(value: V, rules: ValidationRuleSet<V>): ValidationResult {
    val errorMessages = rules.flatMap { rule ->
        when (val result = rule.invoke(value)) {
            is ValidationResult.Valid -> emptyList()
            is ValidationResult.Invalid -> result.messages
        }
    }
    return if (errorMessages.isEmpty()) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(errorMessages)
    }
}
