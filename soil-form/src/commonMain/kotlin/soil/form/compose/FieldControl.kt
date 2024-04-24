// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldPolicy
import soil.form.FieldValidateOn
import soil.form.FormRule

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
    fun validate(value: V = getValue(), dryRun: Boolean = false): Boolean {
        return rule.test(value, dryRun)
    }
}
