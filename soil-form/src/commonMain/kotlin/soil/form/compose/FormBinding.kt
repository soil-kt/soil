// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.annotation.InternalSoilFormApi

/**
 * Internal interface for form binding operations.
 *
 * This interface provides the low-level binding operations between form fields
 * and the form state. It handles field registration, validation, and state management.
 * This is an internal API and should not be used directly by application code.
 *
 * @param T The type of the form data.
 */
@InternalSoilFormApi
@Stable
interface FormBinding<T> {
    /**
     * The current value of the form data.
     */
    val value: T

    /**
     * The form policy that defines validation behavior.
     */
    val policy: FormPolicy

    /**
     * Gets the metadata for a specific field.
     *
     * @param name The name of the field.
     * @return The field metadata, or null if the field is not registered.
     */
    operator fun get(name: FieldName): FieldMetaState?

    /**
     * Sets the metadata for a specific field.
     *
     * @param name The name of the field.
     * @param fieldMeta The field metadata to set.
     */
    operator fun set(name: FieldName, fieldMeta: FieldMetaState)

    /**
     * Registers a field with its validation rule and dependencies.
     *
     * @param name The name of the field.
     * @param dependsOn The set of field names this field depends on.
     * @param rule The validation rule for this field.
     */
    fun register(name: FieldName, dependsOn: FieldNames, rule: FieldRule<T>)

    /**
     * Unregisters a field from the form.
     *
     * @param name The name of the field to unregister.
     */
    fun unregister(name: FieldName)

    /**
     * Validates the form data.
     *
     * @param value The form data to validate.
     * @param dryRun Whether this is a dry run (doesn't update field states).
     * @return True if validation passes, false otherwise.
     */
    fun validate(value: T, dryRun: Boolean): Boolean

    /**
     * Revalidates fields that depend on the specified field.
     *
     * @param name The name of the field whose dependents should be revalidated.
     */
    fun revalidateDependents(name: FieldName)

    /**
     * Handles a change to the form data.
     *
     * @param updater A function that updates the form data.
     */
    fun handleChange(updater: T.() -> T)
}

/**
 * A functional interface for field validation rules.
 *
 * This interface represents a validation rule that can be applied to form data
 * to validate a specific field. It supports both regular validation and dry-run
 * validation for pre-validation scenarios.
 *
 * @param T The type of the form data.
 */
fun interface FieldRule<T> {
    /**
     * Tests the validation rule against the form data.
     *
     * @param value The form data to validate.
     * @param dryRun Whether this is a dry run (doesn't update field states).
     * @return True if validation passes, false otherwise.
     */
    fun test(value: T, dryRun: Boolean): Boolean
}

/**
 * Interface for objects that have access to form binding operations.
 *
 * This interface provides access to the internal form binding for objects
 * that need to interact with the form's low-level operations. This is typically
 * implemented by form-related components and controls.
 *
 * @param T The type of the form data.
 */
@Stable
interface HasFormBinding<T> {
    /**
     * The form binding that provides access to form operations.
     */
    @InternalSoilFormApi
    val binding: FormBinding<T>
}
