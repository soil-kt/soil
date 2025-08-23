// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import soil.form.FieldError
import soil.form.FieldMeta
import soil.form.FieldName
import soil.form.FieldValidationMode
import soil.form.FormData
import soil.form.FormMeta
import soil.form.noFieldError

/**
 * Remembers a form state for the given initial value.
 *
 * Usage:
 * ```kotlin
 * val formState = rememberFormState(
 *     initialValue = FormData()
 * )
 * ```
 *
 * @param T The type of the form value.
 * @param initialValue The initial value of the form.
 * @param saver The saver to save and restore the form state.
 * @return The remembered [form state][FormState].
 */
@Composable
fun <T> rememberFormState(
    initialValue: T,
    saver: Saver<T, Any> = autoSaver(),
    policy: FormPolicy = FormPolicy()
): FormState<T> {
    return rememberSaveable(saver = FormState.Saver(valueSaver = saver, policy = policy)) {
        FormState(value = initialValue, policy = policy)
    }
}

/**
 * A mutable implementation of [FormData] that manages form state.
 *
 * This class provides a concrete implementation of form state management with
 * support for state persistence, validation, and metadata tracking. It integrates
 * with Compose state to provide reactive updates when form data changes.
 *
 * Usage:
 * ```kotlin
 * val formState = FormState(
 *     value = UserData(name = "", email = ""),
 *     policy = FormPolicy()
 * )
 * ```
 *
 * @param T The type of the form data.
 */
@Stable
class FormState<T>(
    value: T,
    override val meta: FormMetaState,
) : FormData<T> {

    /**
     * Creates a new FormState with the specified value and policy.
     *
     * @param value The initial form data value.
     * @param policy The form policy that defines validation behavior.
     */
    constructor(value: T, policy: FormPolicy = FormPolicy()) : this(
        value = value,
        meta = FormMetaState(policy)
    )

    override var value: T by mutableStateOf(value)

    /**
     * Resets the form to a new value and clears all field metadata.
     *
     * This function resets the form data to the specified value and clears all
     * field-level state including errors, dirty flags, touched flags, and validation state.
     * The form submission state is also reset based on the pre-validation policy.
     *
     * Usage:
     * ```kotlin
     * formState.reset(UserData(name = "", email = ""))
     * ```
     *
     * @param newValue The new value to reset the form to.
     */
    inline fun reset(newValue: T) {
        meta.reset { value = newValue }
    }

    /**
     * Sets validation errors for specific fields.
     *
     * This function allows you to programmatically set validation errors for
     * specific fields, typically used for server-side validation errors or
     * custom validation scenarios. Errors are combined with existing field errors.
     *
     * Usage:
     * ```kotlin
     * formState.setError(
     *     "email" to FieldError("Email already exists"),
     *     "username" to FieldError("Username is taken")
     * )
     * ```
     *
     * @param pairs Pairs of field names and their corresponding errors.
     */
    inline fun setError(vararg pairs: Pair<FieldName, FieldError>) {
        meta.setError(*pairs)
    }

    override fun toString(): String {
        return "FormState(value=$value, meta=$meta)"
    }

    companion object {
        fun <T> Saver(valueSaver: Saver<T, Any>, policy: FormPolicy) = Saver<FormState<T>, Any>(
            save = { value ->
                listOf(
                    with(valueSaver) {
                        save(value.value)
                    },
                    with(FormMetaState.Saver(policy)) {
                        save(value.meta)
                    }
                )
            },
            restore = {
                val value = it as List<*>
                FormState(
                    value = with(valueSaver) {
                        @Suppress("UNCHECKED_CAST")
                        restore(checkNotNull(value[0])) as T
                    },
                    meta = with(FormMetaState.Saver(policy)) {
                        restore(checkNotNull(value[1])) as FormMetaState
                    }
                )
            }
        )
    }
}

/**
 * Remembers form metadata state with automatic state restoration.
 *
 * This function is specifically designed for scenarios where your form values contain
 * mutable data types (like `TextFieldState`) that would require custom Saver implementations
 * when using [rememberFormState]. Instead of implementing complex Savers, you can manage
 * field state restoration manually (e.g., with `rememberTextFieldState`) and use this
 * function to handle only the form metadata restoration.
 *
 * **When to use this function:**
 * - Your form contains mutable data types like `TextFieldState`, `MutableState`, etc.
 * - You want to avoid implementing custom Saver logic for complex form data
 * - You prefer to manage individual field state restoration manually
 *
 * **When NOT to use this function:**
 * - Your form uses immutable data classes - use [rememberFormState] instead
 * - You want automatic restoration of both form data and metadata together
 *
 * Usage pattern:
 * ```kotlin
 * @Composable
 * fun MyForm() {
 *     // Manage field state restoration manually
 *     val nameState = rememberTextFieldState()
 *     val emailState = rememberTextFieldState()
 *
 *     // Use rememberFormMetaState for metadata only
 *     val formMeta = rememberFormMetaState(
 *         policy = FormPolicy()
 *     )
 *
 *     // Create FormState with remember (no custom Saver needed)
 *     val formState = remember(formMeta.key) {
 *         FormState(
 *             value = FormData(name = nameState, email = emailState),
 *             meta = formMeta
 *         )
 *     }
 * }
 * ```
 *
 * @param policy The form policy that defines validation behavior and form options.
 * @return The remembered [FormMetaState] with automatic state restoration.
 *
 * @see rememberFormState For immutable form data that can use automatic Savers.
 */
@Composable
fun rememberFormMetaState(
    policy: FormPolicy = FormPolicy()
): FormMetaState {
    return rememberSaveable(saver = FormMetaState.Saver(policy = policy)) {
        FormMetaState(policy = policy)
    }
}

/**
 * A mutable implementation of [FormMeta] that tracks form-level metadata.
 *
 * This class manages form-wide metadata including field states and submission readiness.
 * It provides mutable access to form metadata for internal form management operations.
 */
@Stable
class FormMetaState internal constructor(
    val policy: FormPolicy,
    fields: Map<FieldName, FieldMetaState>,
    canSubmit: Boolean,
    resetCount: Int
) : FormMeta {

    constructor(policy: FormPolicy = FormPolicy()) : this(
        policy = policy,
        fields = emptyMap(),
        canSubmit = !policy.formOptions.preValidation,
        resetCount = 0
    )

    override val fields: MutableMap<FieldName, FieldMetaState> =
        mutableMapOf(*fields.map { (k, v) -> k to v }.toTypedArray())

    override var canSubmit: Boolean by mutableStateOf(canSubmit)

    private var resetCount: Int by mutableIntStateOf(resetCount)

    internal val key: Int get() = resetCount

    /**
     * Resets the form metadata and executes a custom reset block for synchronization.
     *
     * This function is specifically designed for scenarios where you manage mutable form values
     * manually (like `TextFieldState`, `MutableState`, etc.) and need to synchronize their reset
     * operations with the form metadata reset. The provided block allows you to reset your
     * manually managed mutable values at the same time as the form metadata.
     *
     * **Use Case:**
     * When using [rememberFormMetaState] with manually managed field states, you need a way to
     * reset both the form metadata and your custom field states simultaneously. This function
     * provides that synchronization mechanism.
     *
     * **What this function does:**
     * 1. Executes the provided block (where you reset your mutable values)
     * 2. Clears all field metadata (errors, dirty flags, touched flags, validation state)
     * 3. Resets the form submission state based on pre-validation policy
     * 4. Increments the reset counter (which updates the `key` property)
     *
     * Usage pattern:
     * ```kotlin
     * @Composable
     * fun MyForm() {
     *     val nameState = rememberTextFieldState()
     *     val emailState = rememberTextFieldState()
     *     val formMeta = rememberFormMetaState()
     *
     *     val formState = remember(formMeta.key) {
     *         FormState(
     *             value = FormData(name = nameState, email = emailState),
     *             meta = formMeta
     *         )
     *     }
     *
     *     // Reset both form metadata and field states synchronously
     *     fun resetForm() {
     *         formMeta.reset {
     *             nameState.clearText()
     *             emailState.clearText()
     *         }
     *     }
     * }
     * ```
     *
     * @param block A lambda function where you should reset your manually managed mutable values.
     *              This block is executed within a Compose snapshot to ensure atomic updates.
     */
    fun reset(block: () -> Unit) {
        Snapshot.withMutableSnapshot {
            block()
            fields.clear()
            canSubmit = !policy.formOptions.preValidation
            resetCount += 1
        }
    }

    /**
     * Sets validation errors for specific fields.
     *
     * This function allows you to programmatically set validation errors for
     * specific fields, typically used for server-side validation errors or
     * custom validation scenarios. Errors are combined with existing field errors.
     *
     * Usage:
     * ```kotlin
     * formMetaState.setError(
     *     "email" to FieldError("Email already exists"),
     *     "username" to FieldError("Username is taken")
     * )
     * ```
     *
     * @param pairs Pairs of field names and their corresponding errors.
     */
    fun setError(vararg pairs: Pair<FieldName, FieldError>) {
        Snapshot.withMutableSnapshot {
            pairs.forEach { (fieldName, fieldError) ->
                fields[fieldName]?.let { fieldMeta ->
                    fieldMeta.error = fieldError + fieldMeta.error
                }
            }
        }
    }

    override fun toString(): String {
        return "FormMetaState(fields=$fields, canSubmit=$canSubmit, key=$key)"
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun Saver(policy: FormPolicy) = Saver<FormMetaState, Any>(
            save = { value ->
                listOf(
                    value.fields.mapValues {
                        with(FieldMetaState.Saver()) {
                            save(it.value)
                        }
                    },
                    value.canSubmit,
                    value.resetCount
                )
            },
            restore = {
                val value = it as List<*>
                FormMetaState(
                    policy = policy,
                    fields = with(FieldMetaState.Saver()) {
                        (value[0] as Map<FieldName, Any>).mapValues { (_, v) ->
                            restore(v) as FieldMetaState
                        }
                    },
                    canSubmit = value[1] as Boolean,
                    resetCount = value[2] as Int
                )
            }
        )
    }
}

/**
 * A mutable implementation of [FieldMeta] that tracks field-level metadata.
 *
 * This class manages individual field metadata including validation errors,
 * validation mode, and user interaction state. It provides mutable access
 * to field metadata for internal form field management operations.
 */
@Stable
class FieldMetaState internal constructor(
    error: FieldError = noFieldError,
    mode: FieldValidationMode = FieldValidationMode.Blur,
    isTouched: Boolean = false,
    isValidated: Boolean = false
) : FieldMeta {

    override var error: FieldError by mutableStateOf(error)
    override var mode: FieldValidationMode by mutableStateOf(mode)
    override var isTouched: Boolean by mutableStateOf(isTouched)
    override var isValidated: Boolean by mutableStateOf(isValidated)

    override fun toString(): String {
        return "FieldMetaState(error=$error, mode=$mode, isTouched=$isTouched, isValidated=$isValidated)"
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun Saver() = Saver<FieldMetaState, Any>(
            save = { value ->
                listOf(
                    value.error.messages,
                    value.mode,
                    value.isTouched,
                    value.isValidated
                )
            },
            restore = {
                val value = it as List<*>
                FieldMetaState(
                    error = FieldError(value[0] as List<String>),
                    mode = value[1] as FieldValidationMode,
                    isTouched = value[2] as Boolean,
                    isValidated = value[3] as Boolean
                )
            }
        )
    }
}
