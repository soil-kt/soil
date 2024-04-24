// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

interface Submission {
    val isDirty: Boolean
    val hasError: Boolean
    val isSubmitting: Boolean
    val isSubmitted: Boolean
    val submitCount: Int
    val canSubmit: Boolean
    val onSubmit: () -> Unit
}
