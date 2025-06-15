// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.jvm.JvmInline

/**
 * Represents validation errors for a form field.
 *
 * This value class wraps a list of error messages that can occur during field validation.
 * It provides convenient constructors and operators for working with validation errors.
 *
 * Usage:
 * ```kotlin
 * val error = FieldError("Field is required")
 * val multipleErrors = FieldError(listOf("Too short", "Invalid format"))
 * val combinedErrors = error + multipleErrors
 * ```
 *
 * @property messages The list of validation error messages.
 */
@JvmInline
value class FieldError(val messages: List<String>) {
    /**
     * Creates a FieldError with a single error message.
     *
     * @param message The error message.
     */
    constructor(message: String) : this(listOf(message))

    /**
     * Combines this FieldError with another FieldError.
     *
     * @param other The other FieldError to combine with.
     * @return A new FieldError containing all error messages from both instances.
     */
    operator fun plus(other: FieldError): FieldError {
        return FieldError(messages + other.messages)
    }
}

/**
 * A constant representing no validation errors.
 *
 * This is used as a sentinel value to indicate that a field has passed validation
 * and has no errors. It's more efficient than creating new empty FieldError instances.
 *
 * Usage:
 * ```kotlin
 * val validator: FieldValidator<String> = { value ->
 *     if (value.isNotBlank()) noFieldError
 *     else FieldError("Field is required")
 * }
 * ```
 */
val noFieldError: FieldError = FieldError(emptyList())
