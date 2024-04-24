// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FormFieldNames
import soil.form.FormRule
import soil.form.SubmissionPolicy

@Stable
class SubmissionControl<T>(
    val policy: SubmissionPolicy,
    val rule: FormRule<T>,
    val submit: () -> Unit,
    val initialValue: T,
    val getValue: () -> T,
    val hasError: () -> Boolean,
    val getFieldKeys: () -> FormFieldNames,
    val isSubmitting: () -> Boolean,
    val isSubmitted: () -> Boolean,
    val getSubmitCount: () -> Int
) {
    fun validate(value: T, dryRun: Boolean = false): Boolean {
        return rule.test(value, dryRun)
    }
}
