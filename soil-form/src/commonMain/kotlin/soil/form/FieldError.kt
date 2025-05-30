// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.jvm.JvmInline

@JvmInline
value class FieldError(val messages: List<String>) {
    constructor(message: String) : this(listOf(message))

    operator fun plus(other: FieldError): FieldError {
        return FieldError(messages + other.messages)
    }
}

/**
 * Syntax sugar representing that there are no errors in the field.
 */
val noFieldError: FieldError = FieldError(emptyList())
