// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FormFieldNames
import soil.form.FormRule
import soil.form.SubmissionPolicy

/**
 * Represents a submission control in a form.
 *
 * @param T The type of the form value.
 * @property policy The policy of the submission.
 * @property rule The rule to validate the form value.
 * @property submit The function to submit the form.
 * @property initialValue The initial value of the form.
 * @property getValue The function to get the current form value.
 * @property hasError The function to determine if the form has errors.
 * @property getFieldKeys The function to get the keys of the form fields.
 * @property isSubmitting The function to determine if the form is currently submitting.
 * @property isSubmitted The function to determine if the form has been submitted.
 * @property getSubmitCount The function to get the number of times submission process was called.
 * @constructor Creates a submission control.
 */
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

    /**
     * Validates the form value.
     *
     * @param value The value to validate. Defaults to the current form value.
     * @param dryRun Whether to perform a dry run. Defaults to `false`. If `true`, the form errors are not updated.
     * @return `true` if the form value is valid; `false` otherwise.
     */
    fun validate(value: T, dryRun: Boolean = false): Boolean {
        return rule.test(value, dryRun)
    }
}
