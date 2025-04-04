// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormRule
import soil.form.ValidationRuleBuilder
import soil.form.ValidationRuleSet
import soil.form.rules

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
fun <T : Any, V> FormScope<T>.rememberFieldControl(
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
 * Remembers a field control for the given field name with the given rule set.
 *
 * Usage:
 * ```kotlin
 * rememberFieldRuleControl(
 *     name = "First name",
 *     select = { firstName },
 *     update = { copy(firstName = it) }
 * ) {
 *     notBlank { "must be not blank" }
 * }
 * ```
 *
 * @param T The type of the form value.
 * @param V The type of the field value.
 * @param name The name of the field.
 * @param select The function to select the field value.
 * @param update The function to update the field value.
 * @param enabled The function to determine if the field is enabled.
 * @param dependsOn The set of field names that this field depends on.
 * @param builder The block to build the rule set.
 */
@Composable
fun <T : Any, V> FormScope<T>.rememberFieldRuleControl(
    name: FieldName,
    select: T.() -> V,
    update: T.(V) -> T,
    enabled: T.() -> Boolean = { true },
    dependsOn: Set<FieldName>? = null,
    builder: ValidationRuleBuilder<V>.() -> Unit
): FieldControl<V> {
    return rememberFieldRuleControl(
        name = name,
        select = select,
        update = update,
        enabled = enabled,
        dependsOn = dependsOn,
        ruleSet = remember(state) { rules(builder) }
    )
}

/**
 * Remembers a field control for the given field name with the given rule set.
 *
 * Usage:
 * ```kotlin
 * val ruleSet = rules<String> {
 *     notBlank { "must be not blank" }
 * }
 *
 * rememberFieldRuleControl(
 *     name = "First name",
 *     select = { firstName },
 *     update = { copy(firstName = it) },
 *     ruleSet = ruleSet
 * )
 * ```
 *
 * @param T The type of the form value.
 * @param V The type of the field value.
 * @param name The name of the field.
 * @param select The function to select the field value.
 * @param update The function to update the field value.
 * @param enabled The function to determine if the field is enabled.
 * @param dependsOn The set of field names that this field depends on.
 * @param ruleSet The rule set to validate the field value.
 */
@Composable
fun <T : Any, V> FormScope<T>.rememberFieldRuleControl(
    name: FieldName,
    select: T.() -> V,
    update: T.(V) -> T,
    enabled: T.() -> Boolean = { true },
    dependsOn: Set<FieldName>? = null,
    ruleSet: ValidationRuleSet<V>,
): FieldControl<V> {
    val handleValidate = remember<T.() -> FieldErrors?>(state) {
        {
            val currentValue = select()
            ruleSet.flatMap { it.test(currentValue) }
        }
    }
    return rememberFieldControl(
        name = name,
        select = select,
        update = update,
        enabled = enabled,
        dependsOn = dependsOn,
        validate = handleValidate,
    )
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
 * @return The remembered [submission control][SubmissionControl].
 */
@Composable
fun <T : Any> FormScope<T>.rememberSubmissionControl(): SubmissionControl<T> {
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
            rules = getRules,
            submit = handleSubmit,
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

/**
 * Remembers a submission rule control that automatically controls state of the form.
 */
@Composable
fun <T : Any> FormScope<T>.rememberSubmissionRuleAutoControl(): SubmissionControl<T> {
    return rememberSubmissionControl()
}

/**
 * Remembers a watch value that automatically updates when the form state changes.
 *
 * @param T The type of the form value.
 * @param V The type of the watch value.
 * @param select The function to select the watch value.
 */
@Composable
fun <T : Any, V> FormScope<T>.rememberWatch(select: T.() -> V): V {
    val value by remember { derivedStateOf { with(state.value) { select() } } }
    return value
}
