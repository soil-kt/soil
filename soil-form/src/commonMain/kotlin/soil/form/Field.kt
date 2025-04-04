// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * A field is a single input in a form. It has a name, a value, and a set of errors.
 *
 * @param V The type of the value of the field.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
interface Field<V> {

    /**
     * The name of the field.
     */
    val name: FieldName

    /**
     * The value of the field.
     */
    val value: V

    /**
     * The errors of the field. If the field has no errors, this should be an empty list.
     */
    val errors: FieldErrors

    /**
     * Returns `true` if the field is dirty, `false` otherwise.
     * A field is dirty if its value has changed since the initial value.
     */
    val isDirty: Boolean

    /**
     * Returns `true` if the field is enabled, `false` otherwise.
     * A field is enabled if it is not disabled.
     */
    val isEnabled: Boolean

    /**
     * Returns `true` if the field is touched, `false` otherwise.
     * Sets to touched when the field loses focus after being focused.
     */
    val isTouched: Boolean

    /**
     * Returns `true` if the field is focused, `false` otherwise.
     * Sets to focused when the field gains focus.
     */
    val isFocused: Boolean

    /**
     * Callback to notify when the value of the field is changed.
     */
    val onChange: (V) -> Unit

    /**
     * Callback to notify when the field gains focus.
     */
    val onFocus: () -> Unit

    /**
     * Callback to notify when the field loses focus.
     */
    val onBlur: () -> Unit

    /**
     * Returns `true` if the field has errors, `false` otherwise.
     */
    val hasError: Boolean get() = errors.isNotEmpty()

    /**
     * Triggers validation of the field.
     *
     * **NOTE:*
     * This is an escape hatch intended for cases where you want to execute similar validation as onBlur while KeyboardActions are set.
     * It is not intended for use in other contexts.
     */
    fun virtualTrigger(validateOn: FieldValidateOn)
}


/**
 * Represents multiple error messages in the field.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FieldErrors = List<String>

/**
 * Creates error messages for a field.
 *
 * @param messages Error messages. There must be at least one error message.
 * @return The generated error messages for the field.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
fun fieldError(vararg messages: String): FieldErrors {
    require(messages.isNotEmpty())
    return listOf(*messages)
}

/**
 * Syntax sugar representing that there are no errors in the field.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
val noErrors: FieldErrors = emptyList()
