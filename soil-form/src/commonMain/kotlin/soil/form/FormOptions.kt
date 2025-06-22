// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Configuration options for form-level validation behavior.
 *
 * This interface defines form-wide validation settings, particularly around pre-validation
 * which allows the form to validate all fields and determine submission readiness before
 * the user attempts to submit.
 *
 * Usage:
 * ```kotlin
 * val formOptions = FormOptions(
 *     preValidation = true,
 *     preValidationDelayOnMount = 300.milliseconds,
 *     preValidationDelayOnChange = 250.milliseconds
 * )
 * ```
 */
interface FormOptions {
    /**
     * Whether to enable pre-validation of the entire form.
     * When enabled, the form continuously validates all fields to determine if submission is allowed.
     */
    val preValidation: Boolean

    /**
     * The delay before pre-validation is triggered when fields are added or removed.
     * This helps avoid excessive validation calls during field structure changes.
     */
    val preValidationDelayOnMount: Duration

    /**
     * The delay before pre-validation is triggered when any field value changes.
     * This helps prevent excessive validation calls during rapid form interactions.
     */
    val preValidationDelayOnChange: Duration

    /**
     * Default form options with sensible defaults for most use cases.
     */
    companion object Default : FormOptions {
        override val preValidation: Boolean = true
        override val preValidationDelayOnMount: Duration = 200.milliseconds
        override val preValidationDelayOnChange: Duration = 400.milliseconds
    }
}

/**
 * Creates a [FormOptions] instance with the specified configuration.
 *
 * This factory function allows you to customize form-level validation behavior,
 * particularly around pre-validation timing and enablement. All parameters have
 * sensible defaults.
 *
 * Usage:
 * ```kotlin
 * val customFormOptions = FormOptions(
 *     preValidation = false, // Disable pre-validation
 *     preValidationDelayOnMount = 500.milliseconds,
 *     preValidationDelayOnChange = 300.milliseconds
 * )
 * ```
 *
 * @param preValidation Whether to enable form pre-validation. Defaults to true.
 * @param preValidationDelayOnMount The delay before pre-validation when fields are added or removed. Defaults to 200ms.
 * @param preValidationDelayOnChange The delay before pre-validation on value change. Defaults to 400ms.
 * @return A [FormOptions] instance with the specified configuration.
 */
fun FormOptions(
    preValidation: Boolean = FormOptions.preValidation,
    preValidationDelayOnMount: Duration = FormOptions.preValidationDelayOnMount,
    preValidationDelayOnChange: Duration = FormOptions.preValidationDelayOnChange
): FormOptions {
    return object : FormOptions {
        override val preValidation: Boolean = preValidation
        override val preValidationDelayOnMount: Duration = preValidationDelayOnMount
        override val preValidationDelayOnChange: Duration = preValidationDelayOnChange
    }
}
