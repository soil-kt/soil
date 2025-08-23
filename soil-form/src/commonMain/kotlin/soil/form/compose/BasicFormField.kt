// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import soil.form.FieldError
import soil.form.FieldName
import soil.form.FieldValidationMode
import soil.form.noFieldError

interface BasicFormField {
    /**
     * The unique name identifier for this field within the form.
     */
    val name: FieldName

    /**
     * The current validation error for this field, if any.
     */
    val error: FieldError

    /**
     * Whether the field has been touched (focused and then blurred) by the user.
     */
    val isTouched: Boolean

    /**
     * Whether the field currently has focus.
     */
    val isFocused: Boolean

    /**
     * Whether the field is enabled for user interaction.
     */
    val isEnabled: Boolean

    /**
     * Marks the field as focused.
     */
    fun onFocus()

    /**
     * Marks the field as blurred (not focused) and touched.
     */
    fun onBlur()

    /**
     * Handles focus state changes by calling onFocus() or onBlur() as appropriate.
     *
     * @param hasFocus Whether the field currently has focus.
     */
    fun handleFocus(hasFocus: Boolean)

    /**
     * Manually triggers validation for this field with the specified mode.
     *
     * @param mode The validation mode to trigger.
     * @return True if validation was triggered, false if it was not needed.
     */
    fun trigger(mode: FieldValidationMode): Boolean
}

/**
 * Whether the field currently has any validation errors.
 *
 * This is a convenience property that returns true when the field has validation
 * error messages, false otherwise. It's commonly used to conditionally apply
 * error styling to UI components.
 */
val BasicFormField.hasError: Boolean get() = error != noFieldError
