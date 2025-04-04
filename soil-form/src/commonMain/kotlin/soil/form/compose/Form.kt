// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FormData
import soil.form.FormOptions
import soil.form.annotation.InternalSoilFormApi

/**
 * A form interface that provides form state management and submission handling.
 *
 * This interface represents a form with type-safe data binding and validation capabilities.
 * It manages form state, field validation, and form submission logic.
 *
 * Usage:
 * ```kotlin
 * val form = rememberForm(
 *     initialValue = FormData(),
 *     onSubmit = { data -> /* handle submission */ }
 * )
 * ```
 *
 * @param T The type of the form data.
 */
@Stable
interface Form<T> : HasFormBinding<T> {
    /**
     * The current state of the form including data and metadata.
     */
    val state: FormData<T>

    /**
     * Handles form submission by validating all fields and calling the submit callback
     * if validation passes.
     */
    fun handleSubmit()
}

/**
 * Remembers a form with the given initial value and submission handler.
 *
 * This function creates and remembers a form instance that manages form state,
 * validation, and submission. The form state is automatically saved and restored
 * across configuration changes.
 *
 * Usage:
 * ```kotlin
 * val form = rememberForm(
 *     initialValue = UserData(name = "", email = ""),
 *     onSubmit = { userData ->
 *         // Handle form submission
 *         submitUserData(userData)
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param initialValue The initial value for the form.
 * @param saver The saver used to save and restore the form state across configuration changes.
 * @param policy The form policy that defines validation behavior and options.
 * @param onSubmit The callback function called when the form is submitted successfully.
 * @return A [Form] instance that manages the form state and submission.
 */
@Composable
fun <T> rememberForm(
    initialValue: T,
    saver: Saver<T, Any> = autoSaver(),
    policy: FormPolicy = FormPolicy(),
    onSubmit: (T) -> Unit
): Form<T> = rememberForm(
    state = rememberFormState(initialValue, saver, policy),
    onSubmit = onSubmit
)

/**
 * Remembers a form with the given form state and submission handler.
 *
 * This overload allows you to provide your own form state instance, giving you
 * more control over state management and persistence.
 *
 * Usage:
 * ```kotlin
 * val formState = rememberFormState(initialValue = UserData())
 * val form = rememberForm(
 *     state = formState,
 *     onSubmit = { userData ->
 *         // Handle form submission
 *         submitUserData(userData)
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param state The form state to use for this form.
 * @param onSubmit The callback function called when the form is submitted successfully.
 * @return A [Form] instance that manages the form state and submission.
 */
@OptIn(FlowPreview::class)
@Composable
fun <T> rememberForm(
    state: FormState<T>,
    onSubmit: (T) -> Unit
): Form<T> {
    val control = remember(state, state.resetKey) {
        FormController(state = state, onSubmit = onSubmit)
    }
    if (control.options.preValidation) {
        LaunchedEffect(control) {
            // validateOnMount
            launch {
                snapshotFlow { control.fields }
                    .debounce(control.options.preValidationDelayOnMount)
                    .collect {
                        control.preValidate(value = state.value)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { state.value }
                    .drop(1) // Skip the initial value
                    .debounce(control.options.preValidationDelayOnChange)
                    .collect {
                        control.preValidate(value = it)
                    }
            }
        }
    }
    return control
}

@OptIn(InternalSoilFormApi::class)
internal class FormController<T>(
    override val state: FormState<T>,
    private val onSubmit: (T) -> Unit
) : Form<T>, FormBinding<T> {

    override val binding: FormBinding<T> = this

    private val rules = mutableStateMapOf<FieldName, FieldRule<T>>()

    private val dependencies = mutableStateMapOf<FieldName, FieldNames>()

    private val watchers by derivedStateOf {
        dependencies.keys.flatMap { key -> dependencies[key]?.map { Pair(key, it) } ?: emptyList() }
            .groupBy(keySelector = { it.second }, valueTransform = { it.first })
            .mapValues { (_, value) -> value.toSet() }
    }

    val options: FormOptions get() = state.policy.formOptions
    val fields: FieldNames get() = rules.keys

    fun preValidate(value: T) {
        state.meta.canSubmit = validate(value = value, dryRun = true)
    }

    override fun handleSubmit() {
        if (validate(state.value, false)) {
            onSubmit(state.value)
        }
    }

    // ----- FormBinding ----- //

    override val value: T get() = state.value

    override val policy: FormPolicy get() = state.policy

    override fun get(name: FieldName): FieldMetaState? {
        return state.meta.fields[name]
    }

    override fun set(name: FieldName, fieldMeta: FieldMetaState) {
        state.meta.fields[name] = fieldMeta
    }

    override fun register(name: FieldName, dependsOn: FieldNames, rule: FieldRule<T>) {
        rules[name] = rule
        dependencies[name] = dependsOn
    }

    override fun unregister(name: FieldName) {
        rules.remove(name)
        dependencies.remove(name)
    }

    override fun validate(value: T, dryRun: Boolean): Boolean {
        return if (dryRun) {
            rules.values.all { it.test(value, dryRun = true) }
        } else {
            // NOTE: Related to FieldValidateOn, make sure to traverse all rules
            rules.values.map { it.test(value, dryRun = false) }.all { it }
        }
    }

    override fun revalidateDependents(name: FieldName) {
        watchers[name]?.forEach { watcher ->
            val isValidated = state.meta.fields[watcher]?.isValidated ?: false
            if (isValidated) {
                rules[watcher]?.test(state.value, dryRun = false)
            }
        }
    }

    override fun handleChange(updater: T.() -> T) {
        state.value = with(state.value) { updater() }
    }
}
