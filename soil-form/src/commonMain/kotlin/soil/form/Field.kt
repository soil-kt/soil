// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

interface Field<V> {
    val name: FieldName
    val value: V
    val errors: FieldErrors
    val isDirty: Boolean
    val isEnabled: Boolean
    val isTouched: Boolean
    val isFocused: Boolean
    val onChange: (V) -> Unit
    val onFocus: () -> Unit
    val onBlur: () -> Unit
    val hasError: Boolean get() = errors.isNotEmpty()

    // NOTE: This is an escape hatch intended for cases where you want to execute similar validation as onBlur while KeyboardActions are set.
    // It is not intended for use in other contexts.
    fun virtualTrigger(validateOn: FieldValidateOn)
}

typealias FieldName = String
typealias FieldError = String
typealias FieldErrors = List<FieldError>

fun fieldError(vararg messages: String): FieldErrors {
    require(messages.isNotEmpty())
    return listOf(*messages)
}

val noErrors: FieldErrors = emptyList()
