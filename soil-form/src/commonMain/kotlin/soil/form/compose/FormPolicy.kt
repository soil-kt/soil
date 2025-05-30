// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldOptions
import soil.form.FieldValidationMode
import soil.form.FieldValidationStrategy
import soil.form.FormOptions

@Stable
interface FormPolicy {
    val formOptions: FormOptions
    val fieldOptions: FieldOptions

    companion object
}

fun FormPolicy(
    formOptions: FormOptions = FormOptions,
    fieldOptions: FieldOptions = FieldOptions
): FormPolicy {
    return object : FormPolicy {
        override val formOptions: FormOptions = formOptions
        override val fieldOptions: FieldOptions = fieldOptions
    }
}

val FormPolicy.Companion.Minimal: FormPolicy
    get() = FormPolicy(
        formOptions = FormOptions(
            preValidation = false
        ),
        fieldOptions = FieldOptions(
            validationStrategy = FieldValidationStrategy(FieldValidationMode.Submit)
        )
    )
