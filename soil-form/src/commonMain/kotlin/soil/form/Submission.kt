// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * A submission represents the submission control of a form.
 */
interface Submission {

    /**
     * Returns `true` if the field is dirty, `false` otherwise.
     * A form value is dirty if its value has changed since the initial value.
     */
    val isDirty: Boolean

    /**
     * Returns `true` if the form has validation error, `false` otherwise.
     * A form is error if any of its fields has validation error.
     */
    val hasError: Boolean

    /**
     * Returns `true` if the form is submitting, `false` otherwise.
     * This is useful to prevent duplicate submissions until the submission process is completed.
     */
    val isSubmitting: Boolean

    /**
     * Returns `true` if the form is submitted, `false` otherwise.
     */
    val isSubmitted: Boolean

    /**
     * Returns the number of times submission process was called.
     */
    val submitCount: Int

    /**
     * Returns `true` if the form can be submitted, `false` otherwise.
     * This is useful when you want to disable the submit button itself until field validation errors are resolved.
     */
    val canSubmit: Boolean

    /**
     * A callback to notify that the submit button has been clicked.
     */
    val onSubmit: () -> Unit
}
