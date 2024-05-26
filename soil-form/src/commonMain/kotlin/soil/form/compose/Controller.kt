// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import soil.form.Field
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.Submission

/**
 * A controller for a form [field control][FieldControl].
 *
 * To minimize the impact of re-composition due to updates in input values,
 * the [FieldControl] is passed to a [Controller], which then connects the actual input component with the [Field] interface.
 *
 * Usage:
 * ```kotlin
 * Form(..) {
 *   ..
 *     Controller(control = rememberFirstNameFieldControl()) { field ->
 *       TextField(value = field.value, onValueChange = field.onChange, ...)
 *     }
 * }
 * ```
 *
 * @param V The type of the field value.
 * @param control The field control to be managed.
 * @param content The content to be displayed.
 */
@OptIn(FlowPreview::class)
@Composable
fun <V> Controller(
    control: FieldControl<V>,
    content: @Composable (Field<V>) -> Unit
) {
    val current = remember(control) { derivedStateOf { control.getValue() } }
    val errors = remember(control) { derivedStateOf { control.getErrors() } }
    val isDirty = remember(control) { derivedStateOf { current.value != control.defaultValue } }
    val isEnabled = remember(control) { derivedStateOf { control.isEnabled() } }
    val isTouched = rememberSaveable(control) { mutableStateOf(false) }
    val isFocused = remember(control) { mutableStateOf(false) }
    val onFocus = remember(control) {
        {
            isFocused.value = true
        }
    }
    val onBlur = remember(control) {
        {
            isTouched.value = isTouched.value || isFocused.value
            isFocused.value = false
        }
    }
    val field = remember(control) {
        FieldController(
            name = control.name,
            control = control,
            onChange = control.setValue,
            onFocus = onFocus,
            onBlur = onBlur,
            value = current,
            errors = errors,
            isDirty = isDirty,
            isEnabled = isEnabled,
            isTouched = isTouched,
            isFocused = isFocused
        )
    }
    content(field)
    LaunchedEffect(control) {
        // validateOnMount
        launch {
            delay(control.policy.validationDelay.onMount)
            if (control.shouldTrigger(FieldValidateOn.Mount)) {
                control.validate(current.value)
            }
        }

        // validateOnChange
        launch {
            snapshotFlow { current.value }
                .dropWhile { it == control.defaultValue }
                .debounce(control.policy.validationDelay.onChange)
                .collect {
                    if (control.shouldTrigger(FieldValidateOn.Change)) {
                        control.validate(it)
                    }
                }
        }

        // validateOnBlur
        launch {
            snapshotFlow { isFocused.value }
                .scan(Pair(false, false)) { acc, value -> Pair(acc.second, value) }
                // isFocused: true -> false
                .filter { it.first && !it.second }
                .debounce(control.policy.validationDelay.onBlur)
                .collect {
                    if (control.shouldTrigger(FieldValidateOn.Blur)) {
                        control.validate(current.value)
                    }
                }
        }

        launch {
            snapshotFlow { isEnabled.value }
                .collect {
                    if (control.shouldTrigger(FieldValidateOn.Change)) {
                        control.validate(current.value)
                    }
                }
        }
    }
}

@Stable
internal class FieldController<V>(
    override val name: FieldName,
    private val control: FieldControl<V>,
    override val onChange: (V) -> Unit,
    override val onFocus: () -> Unit,
    override val onBlur: () -> Unit,
    value: State<V>,
    errors: State<FieldErrors>,
    isDirty: State<Boolean>,
    isEnabled: State<Boolean>,
    isTouched: State<Boolean>,
    isFocused: State<Boolean>,
) : Field<V> {
    override val value: V by value
    override val errors: FieldErrors by errors
    override val isDirty: Boolean by isDirty
    override val isEnabled: Boolean by isEnabled
    override val isTouched: Boolean by isTouched
    override val isFocused: Boolean by isFocused

    override fun virtualTrigger(validateOn: FieldValidateOn) {
        if (control.shouldTrigger(validateOn)) {
            control.validate()
        }
    }
}

/**
 * A controller for a form [submission control][SubmissionControl].
 *
 * To minimize the impact of re-composition due to updates in input values,
 * the [SubmissionControl] is passed to a [Controller], which then connects the actual input component with the [Submission] interface.
 *
 * Usage:
 * ```kotlin
 * Form(..) {
 *   ..
 *     Controller(control = rememberSubmissionRuleAutoControl()) { submission ->
 *       Button(onClick = submission.onSubmit, enabled = submission.canSubmit, ...)
 *     }
 * }
 * ```
 *
 * @param T The type of the submission value.
 * @param control The submission control to be managed.
 * @param content The content to be displayed.
 */
@OptIn(FlowPreview::class)
@Composable
fun <T> Controller(
    control: SubmissionControl<T>,
    content: @Composable (Submission) -> Unit
) {
    val current = remember(control) { derivedStateOf { control.getValue() } }
    val isDirty = remember(control) { derivedStateOf { current.value != control.initialValue } }
    val hasError = remember(control) { derivedStateOf { control.hasError() } }
    val isSubmitting = remember(control) { derivedStateOf { control.isSubmitting() } }
    val isSubmitted = remember(control) { derivedStateOf { control.isSubmitted() } }
    val submitCount = remember(control) { derivedStateOf { control.getSubmitCount() } }
    val canSubmit = remember(control) { mutableStateOf(!control.policy.preValidation) }
    val submission = remember(control) {
        SubmissionController(
            onSubmit = control.submit,
            isDirty = isDirty,
            hasError = hasError,
            isSubmitting = isSubmitting,
            isSubmitted = isSubmitted,
            submitCount = submitCount,
            canSubmit = canSubmit
        )
    }
    content(submission)
    if (control.policy.preValidation) {
        val fieldKeys by remember { derivedStateOf { control.getFieldKeys() } }
        LaunchedEffect(control) {
            // validateOnMount
            launch {
                snapshotFlow { fieldKeys }
                    .debounce(control.policy.preValidationDelay.onMount)
                    .collect {
                        canSubmit.value = control.validate(current.value, dryRun = true)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { current.value }
                    .dropWhile { it == control.initialValue }
                    .debounce(control.policy.preValidationDelay.onChange)
                    .collect {
                        canSubmit.value = control.validate(it, dryRun = true)
                    }
            }
        }
    }
}

@Stable
internal class SubmissionController(
    override val onSubmit: () -> Unit,
    hasError: State<Boolean>,
    isDirty: State<Boolean>,
    isSubmitting: State<Boolean>,
    isSubmitted: State<Boolean>,
    submitCount: State<Int>,
    canSubmit: State<Boolean>
) : Submission {
    override val isDirty: Boolean by isDirty
    override val hasError: Boolean by hasError
    override val isSubmitting: Boolean by isSubmitting
    override val isSubmitted: Boolean by isSubmitted
    override val submitCount: Int by submitCount
    override val canSubmit: Boolean by canSubmit
}
