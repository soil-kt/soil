// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

enum class FieldValidationMode {
    Mount,
    Change,
    Blur,
    Submit
}

interface FieldValidationStrategy {
    val initial: FieldValidationMode

    fun next(current: FieldValidationMode, isValid: Boolean): FieldValidationMode
}

fun FieldValidationStrategy(
    initial: FieldValidationMode = FieldValidationMode.Blur,
    next: (current: FieldValidationMode, isValid: Boolean) -> FieldValidationMode = defaultStrategy
): FieldValidationStrategy {
    return object : FieldValidationStrategy {
        override val initial: FieldValidationMode = initial

        override fun next(current: FieldValidationMode, isValid: Boolean): FieldValidationMode {
            return next(current, isValid)
        }
    }
}

private val defaultStrategy: (current: FieldValidationMode, isValid: Boolean) -> FieldValidationMode =
    { current, isValid ->
        when (current) {
            FieldValidationMode.Mount -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
            FieldValidationMode.Blur,
            FieldValidationMode.Change,
            FieldValidationMode.Submit -> FieldValidationMode.Change
        }
    }
