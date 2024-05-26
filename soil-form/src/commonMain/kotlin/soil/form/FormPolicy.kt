// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Represents the settings for the overall form policy.
 *
 * @property field Settings related to field policies.
 * @property submission Settings related to submission control.
 * @constructor Creates a new instance of [FormPolicy].
 */
data class FormPolicy(
    val field: FieldPolicy = FieldPolicy(),
    val submission: SubmissionPolicy = SubmissionPolicy()
) {
    companion object {

        /**
         * By default, this setting automatically performs validation after the field loses focus, and before submission.
         */
        val Default = FormPolicy()

        /**
         * This setting performs validation only before submission.
         */
        val Minimal = FormPolicy(
            field = FieldPolicy(
                validationTrigger = FieldValidationTrigger.Submit
            ),
            submission = SubmissionPolicy(
                preValidation = false
            )
        )
    }
}
