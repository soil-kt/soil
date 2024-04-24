// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

data class FormPolicy(
    val field: FieldPolicy = FieldPolicy(),
    val submission: SubmissionPolicy = SubmissionPolicy()
) {
    companion object {
        val Default = FormPolicy()
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
