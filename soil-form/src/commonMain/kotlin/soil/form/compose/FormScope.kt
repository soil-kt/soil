// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormRule
import soil.form.FormRules

/**
 * A scope to manage the state and actions of input fields in a form.
 *
 * @param T The type of the form value.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
@Stable
class FormScope<T : Any> internal constructor(
    private val state: LegacyFormStateImpl<T>,
    private val submitHandler: SubmitHandler<T>
) {

    /**
     * The current form state.
     */
    val formState: LegacyFormState<T> get() = state

    /**
     * Remembers a field control for the given field name.
     *
     * Usage:
     * ```kotlin
     * rememberFieldControl(
     *     name = "First name",
     *     select = { firstName },
     *     update = { copy(firstName = it) }
     * ) {
     *     if (firstName.isNotBlank()) {
     *         noErrors
     *     } else {
     *         fieldError("must be not blank")
     *     }
     * }
     * ```
     *
     * @param V The type of the field value.
     * @param name The name of the field.
     * @param select The function to select the field value.
     * @param update The function to update the field value.
     * @param enabled The function to determine if the field is enabled.
     * @param dependsOn The set of field names that this field depends on.
     * @param validate The function to validate the field value.
     * @return The remembered [field control][FieldControl].
     */
    @Composable
    fun <V> rememberFieldControl(
        name: FieldName,
        select: T.() -> V,
        update: T.(V) -> T,
        enabled: T.() -> Boolean = { true },
        dependsOn: Set<FieldName>? = null,
        validate: (T.() -> FieldErrors?)? = null
    ): FieldControl<V> {
        val defaultValue = remember(state) { with(state.initialValue) { select() } }
        val getValue = remember(state) {
            { with(state.value) { select() } }
        }
        val setValue = remember<(V) -> Unit>(state) {
            { value -> state.value = with(state.value) { update(value) } }
        }
        val getError = remember(state) {
            { state.errors[name] ?: emptyList() }
        }
        val setError = remember<(FieldErrors) -> Unit>(state) {
            { error -> state.errors[name] = error }
        }
        val shouldTrigger = remember<(FieldValidateOn) -> Boolean>(state) {
            { target -> target == state.getTriggerFor(name) }
        }
        val isEnabled = remember(state) {
            { state.value.enabled() }
        }
        val trigger = remember<(T, Boolean) -> Boolean>(state) {
            { value, dryRun ->
                val error = if (isEnabled()) {
                    with(value) { validate?.invoke(this) ?: emptyList() }
                } else {
                    emptyList()
                }
                val isPassed = error.isEmpty()
                if (!dryRun) {
                    state.updateError(name, error)
                    state.revalidateDependsOn(name)
                }
                isPassed
            }
        }
        val formValidationRule = remember<FormRule<T>>(state) {
            FormRule { value, dryRun ->
                trigger(value, dryRun)
            }
        }
        val fieldValidationRule = remember<FormRule<V>>(state) {
            FormRule { value, dryRun ->
                trigger(with(state.value) { update(value) }, dryRun)
            }
        }
        // Submission用途
        DisposableEffect(formValidationRule) {
            state.rules[name] = formValidationRule
            state.dependsOn[name] = dependsOn ?: emptySet()
            onDispose {
                state.rules.remove(name)
                state.dependsOn.remove(name)
            }
        }
        return remember(state) {
            FieldControl(
                name = name,
                policy = state.policy.field,
                rule = fieldValidationRule,
                defaultValue = defaultValue,
                getValue = getValue,
                setValue = setValue,
                getErrors = getError,
                setErrors = setError,
                shouldTrigger = shouldTrigger,
                isEnabled = isEnabled
            )
        }
    }

    /**
     * Remembers a submission control for the form.
     *
     * Usage:
     * ```kotlin
     * rememberSubmissionControl { rules, dryRun ->
     *     rules.values.map { it.test(this, dryRun = dryRun) }.all { it }
     * }
     * ```
     *
     * @param validate The function to validate the form value.
     * @return The remembered [submission control][SubmissionControl].
     */
    @Composable
    fun rememberSubmissionControl(
        validate: T.(rules: FormRules<T>, dryRun: Boolean) -> Boolean
    ): SubmissionControl<T> {
        val getValue = remember(state) {
            { state.value }
        }
        val hasError = remember(state) {
            { state.hasError }
        }
        val getFieldKeys = remember(state) {
            { state.rules.keys }
        }
        val getRules = remember(state) {
            { state.rules.toMap() }
        }
        val validationRule = remember<FormRule<T>>(state) {
            FormRule { value, dryRun -> with(value) { validate(getRules(), dryRun) } }
        }
        val submit = remember(state) {
            { submitHandler.handleSubmit(validationRule) }
        }
        val isSubmitting = remember(state) {
            { state.isSubmitting }
        }
        val isSubmitted = remember(state) {
            { state.isSubmitted }
        }
        val getSubmitCount = remember(state) {
            { state.submitCount }
        }
        return remember(state) {
            SubmissionControl(
                policy = state.policy.submission,
                rule = validationRule,
                submit = submit,
                initialValue = state.initialValue,
                getValue = getValue,
                hasError = hasError,
                getFieldKeys = getFieldKeys,
                isSubmitting = isSubmitting,
                isSubmitted = isSubmitted,
                getSubmitCount = getSubmitCount
            )
        }
    }
}

@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
internal fun interface SubmitHandler<T> {
    fun handleSubmit(rule: FormRule<T>)
}
