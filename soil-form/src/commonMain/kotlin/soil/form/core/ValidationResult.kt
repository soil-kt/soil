// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

/**
 * Represents the result of a validation operation.
 *
 * This sealed class provides a type-safe way to represent validation outcomes,
 * either successful validation or validation failure with error messages.
 *
 * Usage:
 * ```kotlin
 * val result = when {
 *     value.isNotBlank() -> ValidationResult.Valid
 *     else -> ValidationResult.Invalid("Value is required")
 * }
 * ```
 */
sealed class ValidationResult {
    /**
     * Represents a successful validation result with no errors.
     */
    data object Valid : ValidationResult()

    /**
     * Represents a failed validation result with one or more error messages.
     *
     * @property messages The list of validation error messages.
     */
    data class Invalid(val messages: List<String>) : ValidationResult() {
        /**
         * Creates an Invalid result with a single error message.
         *
         * @param message The error message.
         */
        constructor(message: String) : this(listOf(message))
    }
}
