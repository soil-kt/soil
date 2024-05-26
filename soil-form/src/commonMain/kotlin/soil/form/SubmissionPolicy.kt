// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Represents the settings for submission-related policies, such as pre-validation behavior.
 *
 * @property preValidation Whether to perform pre-validation before submission.
 * @property preValidationDelay The settings for delayed execution of pre-validation.
 * @constructor Creates a new instance of [SubmissionPolicy].
 */
data class SubmissionPolicy(
    val preValidation: Boolean = true,
    val preValidationDelay: SubmissionPreValidationDelay = SubmissionPreValidationDelay()
)

/**
 * Represents the settings for delayed execution of pre-validation.
 *
 * **Note:**
 * [onMount] as a zero duration is not recommended as it triggers based on the registration of Field's Rules.
 *
 * @property onMount The delay before the pre-validation when the form is mounted.
 * @property onChange The delay before pre-validation when the form value changes.
 * @constructor Creates a new instance of [SubmissionPreValidationDelay].
 */
data class SubmissionPreValidationDelay(
    val onMount: Duration = 200.milliseconds,
    val onChange: Duration = 200.milliseconds
)
