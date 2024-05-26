// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldPolicy
import soil.form.FieldValidateOn
import soil.form.FormRule

/**
 * Represents a field control in a form.
 *
 * @param V The type of the field value.
 * @property name The name of the field.
 * @property policy The policy of the field.
 * @property rule The rule to validate the field value.
 * @property defaultValue The default value of the field.
 * @property getValue The function to get the current field value.
 * @property setValue The function to set the new field value.
 * @property getErrors The function to get the current field errors.
 * @property setErrors The function to set the new field errors. If the new errors are empty, the field is considered valid.
 * @property shouldTrigger The function to determine if the field should trigger validation on the given event.
 * @property isEnabled The function to determine if the field is enabled.
 */
@Stable
class FieldControl<V>(
    val name: FieldName,
    val policy: FieldPolicy,
    val rule: FormRule<V>,
    val defaultValue: V,
    val getValue: () -> V,
    val setValue: (V) -> Unit,
    val getErrors: () -> FieldErrors,
    val setErrors: (FieldErrors) -> Unit,
    val shouldTrigger: (FieldValidateOn) -> Boolean,
    val isEnabled: () -> Boolean
) {

    /**
     * Validates the field value.
     *
     * @param value The value to validate. Defaults to the current field value.
     * @param dryRun Whether to perform a dry run. Defaults to `false`. If `true`, the field errors are not updated.
     * @return `true` if the field value is valid; `false` otherwise.
     */
    fun validate(value: V = getValue(), dryRun: Boolean = false): Boolean {
        return rule.test(value, dryRun)
    }
}
