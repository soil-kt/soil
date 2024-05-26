// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormErrors
import soil.form.FormFieldDependencies
import soil.form.FormFieldNames
import soil.form.FormPolicy
import soil.form.FormRule
import soil.form.FormRules
import soil.form.FormTriggers

/**
 * The managed state of a form.
 *
 * @param T The type of the form value.
 */
@Stable
interface FormState<T> {

    /**
     * The initial value of the form.
     */
    val initialValue: T

    /**
     * The current value of the form.
     */
    val value: T

    /**
     * Whether the form is currently submitting.
     */
    val isSubmitting: Boolean

    /**
     * Whether the form has been submitted.
     */
    val isSubmitted: Boolean

    /**
     * The number of times submission process was called.
     */
    val submitCount: Int

    /**
     * The validation errors of the form.
     */
    val errors: FormErrors

    /**
     * The validation triggers of the form.
     */
    val triggers: FormTriggers

    /**
     * The validation rules of the form.
     */
    val rules: FormRules<T>

    /**
     * The dependencies of the form fields.
     */
    val dependsOn: FormFieldDependencies

    /**
     * The field dependencies of the form fields.
     */
    val watchers: FormFieldDependencies

    /**
     * Returns `true` if the form has validation error, `false` otherwise.
     */
    val hasError: Boolean
        get() = errors.values.any { it.isNotEmpty() }
}

@Stable
internal class FormStateImpl<T>(
    val policy: FormPolicy,
    override val initialValue: T,
    value: MutableState<T>,
    isSubmitting: MutableState<Boolean>,
    isSubmitted: MutableState<Boolean>,
    submitCount: MutableState<Int>,
    override val errors: SnapshotStateMap<FieldName, FieldErrors>,
    override val triggers: SnapshotStateMap<FieldName, FieldValidateOn>,
    override val rules: SnapshotStateMap<FieldName, FormRule<T>>,
    override val dependsOn: SnapshotStateMap<FieldName, FormFieldNames>,
    watchers: State<FormFieldDependencies>,
) : FormState<T> {
    override var value by value
    override var isSubmitting: Boolean by isSubmitting
    override var isSubmitted: Boolean by isSubmitted
    override var submitCount: Int by submitCount
    override val watchers: FormFieldDependencies by watchers

    internal fun getTriggerFor(field: FieldName): FieldValidateOn {
        return triggers[field] ?: policy.field.validationTrigger.startAt
    }

    internal fun updateError(
        field: FieldName,
        fieldErrors: FieldErrors,
        validateOn: FieldValidateOn = getTriggerFor(field)
    ) {
        errors[field] = fieldErrors
        triggers[field] = policy.field.validationTrigger.next(validateOn, fieldErrors.isEmpty())
    }

    internal fun forceError(errors: FormErrors, validateOn: FieldValidateOn) {
        errors.forEach { (field, fieldErrors) -> updateError(field, fieldErrors, validateOn) }
    }

    internal fun revalidateDependsOn(name: FieldName) {
        watchers[name]?.forEach { field ->
            val hasValidatedOnce = errors.containsKey(field)
            if (hasValidatedOnce) {
                rules[field]?.test(value, dryRun = false)
            }
        }
    }
}
