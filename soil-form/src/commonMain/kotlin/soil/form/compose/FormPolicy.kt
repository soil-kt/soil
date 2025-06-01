// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldOptions
import soil.form.FieldValidationMode
import soil.form.FieldValidationStrategy
import soil.form.FormOptions

/**
 * Defines the policy configuration for form validation behavior.
 *
 * FormPolicy combines both form-level and field-level configuration options
 * to provide a comprehensive validation strategy for forms. It allows you to
 * customize how and when validation occurs at both the form and field levels.
 *
 * Usage:
 * ```kotlin
 * val customPolicy = FormPolicy(
 *     formOptions = FormOptions(
 *         preValidation = true,
 *         preValidationDelayOnChange = 300.milliseconds
 *     ),
 *     fieldOptions = FieldOptions(
 *         validationDelayOnChange = 250.milliseconds
 *     )
 * )
 * ```
 */
@Stable
interface FormPolicy {
    /**
     * Configuration options for form-level validation behavior.
     */
    val formOptions: FormOptions

    /**
     * Configuration options for field-level validation behavior.
     */
    val fieldOptions: FieldOptions

    companion object
}

/**
 * Creates a FormPolicy with the specified form and field options.
 *
 * This factory function allows you to create a custom form policy by combining
 * form-level and field-level configuration options. Both parameters have sensible
 * defaults that work well for most use cases.
 *
 * Usage:
 * ```kotlin
 * val policy = FormPolicy(
 *     formOptions = FormOptions(
 *         preValidation = false
 *     ),
 *     fieldOptions = FieldOptions(
 *         validationStrategy = FieldValidationStrategy(
 *             initial = FieldValidationMode.Change
 *         )
 *     )
 * )
 * ```
 *
 * @param formOptions The form-level validation options. Defaults to [FormOptions] defaults.
 * @param fieldOptions The field-level validation options. Defaults to [FieldOptions] defaults.
 * @return A [FormPolicy] instance with the specified configuration.
 */
fun FormPolicy(
    formOptions: FormOptions = FormOptions,
    fieldOptions: FieldOptions = FieldOptions
): FormPolicy {
    return object : FormPolicy {
        override val formOptions: FormOptions = formOptions
        override val fieldOptions: FieldOptions = fieldOptions
    }
}

/**
 * A minimal form policy that performs validation only on form submission.
 *
 * This policy disables pre-validation and sets field validation to occur only
 * when the form is submitted. This is useful for forms where you want to minimize
 * validation feedback during user input and only validate when the user attempts
 * to submit the form.
 *
 * Usage:
 * ```kotlin
 * val form = rememberForm(
 *     initialValue = FormData(),
 *     policy = FormPolicy.Minimal,
 *     onSubmit = { data -> /* handle submission */ }
 * )
 * ```
 */
val FormPolicy.Companion.Minimal: FormPolicy
    get() = FormPolicy(
        formOptions = FormOptions(
            preValidation = false
        ),
        fieldOptions = FieldOptions(
            validationStrategy = FieldValidationStrategy(FieldValidationMode.Submit)
        )
    )
