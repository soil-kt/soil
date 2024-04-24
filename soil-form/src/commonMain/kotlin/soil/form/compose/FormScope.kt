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


@Stable
class FormScope<T : Any> internal constructor(
    private val state: FormStateImpl<T>,
    private val submitHandler: SubmitHandler<T>
) {

    val formState: FormState<T> get() = state

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

internal fun interface SubmitHandler<T> {
    fun handleSubmit(rule: FormRule<T>)
}
