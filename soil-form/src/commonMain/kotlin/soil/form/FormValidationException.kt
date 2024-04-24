// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

class FormValidationException(
    val errors: FormErrors,
    cause: Throwable? = null
) : RuntimeException("Unverified form ${errors.keys}", cause)
