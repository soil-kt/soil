// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class FieldPolicy(
    val validationTrigger: FieldValidationTrigger = FieldValidationTrigger,
    val validationDelay: FieldValidationDelay = FieldValidationDelay()
)

data class FieldValidationDelay(
    val onMount: Duration = Duration.ZERO,
    val onChange: Duration = 250.milliseconds,
    val onBlur: Duration = Duration.ZERO
)

enum class FieldValidateOn {
    Mount, Change, Blur, Submit
}

interface FieldValidationTrigger {
    val startAt: FieldValidateOn

    fun next(state: FieldValidateOn, isPassed: Boolean): FieldValidateOn

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
