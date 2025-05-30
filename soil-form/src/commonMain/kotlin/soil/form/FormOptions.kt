// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface FormOptions {
    val preValidation: Boolean
    val preValidationDelayOnMount: Duration
    val preValidationDelayOnChange: Duration

    companion object Default : FormOptions {
        override val preValidation: Boolean = true
        override val preValidationDelayOnMount: Duration = 200.milliseconds
        override val preValidationDelayOnChange: Duration = 200.milliseconds
    }
}

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
