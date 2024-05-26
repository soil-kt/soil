// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Represents the settings for field-related policies, such as validation behavior.
 *
 * @property validationTrigger The timing to trigger automatic validation.
 * @property validationDelay The settings for delayed execution of field validation.
 * @constructor Creates a new instance of [FieldPolicy].
 */
data class FieldPolicy(
    val validationTrigger: FieldValidationTrigger = FieldValidationTrigger,
    val validationDelay: FieldValidationDelay = FieldValidationDelay()
)

/**
 * Represents the settings for delayed execution of field validation.
 *
 * @property onMount The delay before the validation when the field mount.
 * @property onChange The delay before validation when the field value changes.
 * @property onBlur The delay before validation when the field loses focus.
 * @constructor Creates a new instance of [FieldValidationDelay].
 */
data class FieldValidationDelay(
    val onMount: Duration = Duration.ZERO,
    val onChange: Duration = 250.milliseconds,
    val onBlur: Duration = Duration.ZERO
)

/**
 * Enumerates the timings when field validation is performed.
 */
enum class FieldValidateOn {

    /** When the field component is mounted */
    Mount,

    /** When the input value changes */
    Change,

    /** When the focus is lost */
    Blur,

    /** When the form is submit */
    Submit
}

/**
 * Represents the timings that trigger field validation.
 */
interface FieldValidationTrigger {

    /**
     * The timing to trigger automatic validation.
     */
    val startAt: FieldValidateOn

    /**
     * Returns the timing to trigger the next validation.
     *
     * @param state The current validation timing
     * @param isPassed Whether the validation was successful
     * @return The next validation timing
     */
    fun next(state: FieldValidateOn, isPassed: Boolean): FieldValidateOn

    /**
     * By default, this setting automatically performs validation after the field loses focus,
     * and if validation fails, it will re-validate each time the form input changes.
     */
    companion object Default : FieldValidationTrigger {
        override val startAt: FieldValidateOn = FieldValidateOn.Blur

        override fun next(state: FieldValidateOn, isPassed: Boolean): FieldValidateOn {
            return when (state) {
                FieldValidateOn.Mount -> {
                    if (isPassed) FieldValidateOn.Blur else FieldValidateOn.Change
                }

                FieldValidateOn.Blur -> {
                    if (isPassed) FieldValidateOn.Blur else FieldValidateOn.Change
                }

                FieldValidateOn.Change,
                FieldValidateOn.Submit -> FieldValidateOn.Change
            }
        }
    }

    /**
     * Automatically performs validation at the time of form submission,
     * and thereafter re-validates each time the form input changes.
     */
    object Submit : FieldValidationTrigger {
        override val startAt: FieldValidateOn = FieldValidateOn.Submit

        override fun next(state: FieldValidateOn, isPassed: Boolean): FieldValidateOn {
            return when (state) {
                FieldValidateOn.Submit -> FieldValidateOn.Change
                else -> FieldValidateOn.Change
            }
        }
    }
}
