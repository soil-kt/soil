// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FormData
import soil.form.FormMeta
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
interface Form<T> : FormData<T>, HasFormBinding<T> {

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
    val control = remember(state, state.meta.key) {
        FormController(state = state, onSubmit = onSubmit)
    }
    if (control.options.preValidation) {
        LaunchedEffect(control) {
            // validateOnMount
            launch {
                snapshotFlow { control.fields.toSet() }
                    .filter { it.isNotEmpty() }
                    .debounce(control.options.preValidationDelayOnMount)
                    .collect {
                        control.preValidate()
                    }
            }

            // validateOnChange
            launch {
                control.fieldChanges
                    .debounce(control.options.preValidationDelayOnChange)
                    .collect {
                        control.preValidate()
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

    private val fieldChangeEmitter = MutableSharedFlow<FieldName>(extraBufferCapacity = Int.MAX_VALUE)

    val options: FormOptions get() = state.meta.policy.formOptions
    val fields: FieldNames get() = rules.keys

    fun preValidate(value: T = state.value) {
        state.meta.canSubmit = validate(value = value, dryRun = true)
    }

    override fun handleSubmit() {
        if (validate(state.value, false)) {
            onSubmit(state.value)
        }
    }

    // ----- FormData ----- //

    override val value: T get() = state.value

    override val meta: FormMeta = state.meta

    // ----- FormBinding ----- //

    override val policy: FormPolicy get() = state.meta.policy

    override val fieldChanges get() = fieldChangeEmitter.asSharedFlow()

    override fun get(name: FieldName): FieldMetaState? {
        return state.meta.fields[name]
    }

    override fun set(name: FieldName, fieldMeta: FieldMetaState) {
        state.meta.fields[name] = fieldMeta
    }

    override fun register(name: FieldName, rule: FieldRule<T>) {
        rules[name] = rule
    }

    override fun unregister(name: FieldName) {
        rules.remove(name)
    }

    override fun validate(value: T, dryRun: Boolean): Boolean {
        return if (dryRun) {
            rules.values.all { it.test(value, dryRun = true) }
        } else {
            // NOTE: Related to FieldValidateOn, make sure to traverse all rules
            rules.values.map { it.test(value, dryRun = false) }.all { it }
        }
    }

    override fun handleChange(updater: T.() -> T) {
        state.value = with(state.value) { updater() }
    }

    override suspend fun notifyFieldChange(name: FieldName) {
        fieldChangeEmitter.emit(name)
    }
}
