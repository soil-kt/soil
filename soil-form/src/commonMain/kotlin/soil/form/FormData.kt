// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Represents the complete state of a form including its data and metadata.
 *
 * This interface provides access to both the form's data value and its metadata,
 * which includes field-level information such as validation errors, dirty state,
 * and submission readiness.
 *
 * @param T The type of the form data.
 */
interface FormData<T> {
    /**
     * The current data value of the form.
     */
    val value: T

    /**
     * The metadata associated with the form, including field states and submission status.
     */
    val meta: FormMeta
}

/**
 * Represents metadata for a form, including field-level information and submission state.
 *
 * This interface provides access to form-wide metadata such as the state of individual
 * fields and whether the form is ready for submission.
 */
interface FormMeta {
    /**
     * A map of field names to their corresponding metadata.
     * Contains information about each field's validation state, errors, and user interactions.
     */
    val fields: Map<FieldName, FieldMeta>

    /**
     * Whether the form is ready for submission.
     * This is typically true when all required fields are valid and any pre-validation has passed.
     */
    val canSubmit: Boolean
}

/**
 * Represents metadata for an individual form field.
 *
 * This interface provides access to field-level state information including
 * validation errors, validation mode, and user interaction state.
 */
interface FieldMeta {
    /**
     * The current validation error for this field, if any.
     * Contains [noFieldError] if the field has no validation errors.
     */
    val error: FieldError

    /**
     * The current validation mode for this field.
     * Determines when validation should be triggered (e.g., on blur, on change).
     */
    val mode: FieldValidationMode

    /**
     * Whether the field has been touched (focused and then blurred) by the user.
     */
    val isTouched: Boolean

    /**
     * Whether validation has been performed on this field at least once.
     */
    val isValidated: Boolean
}
