// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class SubmissionPolicy(
    val preValidation: Boolean = true,
    val preValidationDelay: SubmissionPreValidationDelay = SubmissionPreValidationDelay()
)

data class SubmissionPreValidationDelay(
    // NOTE: As it triggers based on the registration of Field's Rules, a zero duration is not recommended
    val onMount: Duration = 200.milliseconds,
    val onChange: Duration = 200.milliseconds
)
