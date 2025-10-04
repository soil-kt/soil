// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.form.core.ValidationResult
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules
import soil.form.core.validate

/**
 * A type alias for field validation functions.
 *
 * A field validator is a function that takes a field value and returns a [FieldError].
 * If validation passes, it should return [noFieldError]. If validation fails,
 * it should return a [FieldError] containing the validation error messages.
 *
 * Usage:
 * ```kotlin
 * val emailValidator: FieldValidator<String> = { email ->
 *     if (email.contains("@")) noFieldError
 *     else FieldError(listOf("Must be a valid email"))
 * }
 * ```
 *
 * @param V The type of the value being validated.
 */
typealias FieldValidator<V> = (V) -> FieldError

/**
 * Creates a field validator using a validation rule builder.
 *
 * This function provides a convenient DSL for building field validators using
 * predefined validation rules. The builder allows you to chain multiple validation
 * rules together.
 *
 * Usage:
 * ```kotlin
 * val nameValidator = FieldValidator<String> {
 *     notBlank { "Name is required" }
 *     minLength(2) { "Name must be at least 2 characters" }
 *     maxLength(50) { "Name must not exceed 50 characters" }
 * }
 *
 * val emailValidator = FieldValidator<String> {
 *     notBlank { "Email is required" }
 *     match("^[^@]+@[^@]+\\.[^@]+$") { "Must be a valid email address" }
 * }
 * ```
 *
 * @param V The type of the value being validated.
 * @param block A lambda that builds the validation rules using [ValidationRuleBuilder].
 * @return A [FieldValidator] that applies all the specified validation rules.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <V> FieldValidator(
    noinline block: ValidationRuleBuilder<V>.() -> Unit
): FieldValidator<V> = { value ->
    when (val result = validate(value, rules(block))) {
        is ValidationResult.Valid -> noFieldError
        is ValidationResult.Invalid -> FieldError(result.messages)
    }
}
