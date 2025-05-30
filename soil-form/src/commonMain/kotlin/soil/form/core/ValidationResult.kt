// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val messages: List<String>) : ValidationResult() {
        constructor(message: String) : this(listOf(message))
    }
}
