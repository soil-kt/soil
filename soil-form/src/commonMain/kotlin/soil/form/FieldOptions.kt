// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface FieldOptions {
    val validationStrategy: FieldValidationStrategy
    val validationDelayOnMount: Duration
    val validationDelayOnChange: Duration
    val validationDelayOnBlur: Duration

    companion object Default : FieldOptions {
        override val validationStrategy: FieldValidationStrategy = FieldValidationStrategy()
        override val validationDelayOnMount: Duration = Duration.ZERO
        override val validationDelayOnChange: Duration = 250.milliseconds
        override val validationDelayOnBlur: Duration = Duration.ZERO
    }
}

fun FieldOptions(
    validationStrategy: FieldValidationStrategy = FieldOptions.validationStrategy,
    validationDelayOnMount: Duration = FieldOptions.validationDelayOnMount,
    validationDelayOnChange: Duration = FieldOptions.validationDelayOnChange,
    validationDelayOnBlur: Duration = FieldOptions.validationDelayOnBlur
): FieldOptions {
    return object : FieldOptions {
        override val validationStrategy: FieldValidationStrategy = validationStrategy
        override val validationDelayOnMount: Duration = validationDelayOnMount
        override val validationDelayOnChange: Duration = validationDelayOnChange
        override val validationDelayOnBlur: Duration = validationDelayOnBlur
    }
}
