// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
    return rememberSaveable(saver = FormState.Saver(formSaver = saver, formPolicy = policy)) {
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
class FormState<T> internal constructor(
    value: T,
    override val meta: FormMetaState,
    resetKey: Int,
    val policy: FormPolicy
) : FormData<T> {

    /**
     * Creates a new FormState with the specified value and policy.
     *
     * @param value The initial form data value.
     * @param policy The form policy that defines validation behavior.
     */
    constructor(value: T, policy: FormPolicy = FormPolicy()) : this(
        value = value,
        meta = FormMetaState(canSubmit = !policy.formOptions.preValidation),
        resetKey = 0,
        policy = policy
    )

    override var value: T by mutableStateOf(value)

    internal var resetKey: Int by mutableStateOf(resetKey)
        private set

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
    fun reset(newValue: T) {
        Snapshot.withMutableSnapshot {
            value = newValue
            meta.fields.clear()
            meta.canSubmit = !policy.formOptions.preValidation
            resetKey += 1
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
     * formState.setError(
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
                meta.fields[fieldName]?.let { fieldMeta ->
                    fieldMeta.error = fieldError + fieldMeta.error
                }
            }
        }
    }

    override fun toString(): String {
        return "FormState(value=$value, meta=$meta, resetKey=$resetKey, policy=$policy)"
    }

    companion object {
        fun <T> Saver(formSaver: Saver<T, Any>, formPolicy: FormPolicy) = Saver<FormState<T>, Any>(
            save = { value ->
                listOf(
                    with(formSaver) {
                        save(value.value)
                    },
                    with(FormMetaState.Saver()) {
                        save(value.meta)
                    },
                    value.resetKey
                )
            },
            restore = {
                val (value, meta, resetKey) = it as List<*>
                FormState(
                    value = with(formSaver) {
                        @Suppress("UNCHECKED_CAST")
                        restore(checkNotNull(value)) as T
                    },
                    meta = with(FormMetaState.Saver()) {
                        restore(checkNotNull(meta)) as FormMetaState
                    },
                    resetKey = resetKey as Int,
                    policy = formPolicy
                )
            }
        )
    }
}

/**
 * A mutable implementation of [FormMeta] that tracks form-level metadata.
 *
 * This class manages form-wide metadata including field states and submission readiness.
 * It provides mutable access to form metadata for internal form management operations.
 */
class FormMetaState internal constructor(
    fields: Map<FieldName, FieldMetaState> = emptyMap(),
    canSubmit: Boolean = false
) : FormMeta {

    override val fields: MutableMap<FieldName, FieldMetaState> =
        mutableMapOf(*fields.map { (k, v) -> k to v }.toTypedArray())

    override var canSubmit: Boolean by mutableStateOf(canSubmit)

    override fun toString(): String {
        return "FormMetaState(fields=$fields, canSubmit=$canSubmit)"
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun Saver() = Saver<FormMetaState, Any>(
            save = { value ->
                listOf(
                    value.fields.mapValues {
                        with(FieldMetaState.Saver()) {
                            save(it.value)
                        }
                    },
                    value.canSubmit
                )
            },
            restore = {
                val value = it as List<*>
                FormMetaState(
                    fields = with(FieldMetaState.Saver()) {
                        (value[0] as Map<FieldName, Any>).mapValues { (_, v) ->
                            restore(v) as FieldMetaState
                        }
                    },
                    canSubmit = value[1] as Boolean,
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
class FieldMetaState internal constructor(
    error: FieldError = noFieldError,
    mode: FieldValidationMode = FieldValidationMode.Blur,
    isDirty: Boolean = false,
    isTouched: Boolean = false,
    isValidated: Boolean = false
) : FieldMeta {

    override var error: FieldError by mutableStateOf(error)
    override var mode: FieldValidationMode by mutableStateOf(mode)
    override var isDirty: Boolean by mutableStateOf(isDirty)
    override var isTouched: Boolean by mutableStateOf(isTouched)
    override var isValidated: Boolean by mutableStateOf(isValidated)

    override fun toString(): String {
        return "FieldMetaState(error=$error, mode=$mode, isDirty=$isDirty, isTouched=$isTouched, isValidated=$isValidated)"
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun Saver() = Saver<FieldMetaState, Any>(
            save = { value ->
                listOf(
                    value.error.messages,
                    value.mode,
                    value.isDirty,
                    value.isTouched,
                    value.isValidated
                )
            },
            restore = {
                val value = it as List<*>
                FieldMetaState(
                    error = FieldError(value[0] as List<String>),
                    mode = value[1] as FieldValidationMode,
                    isDirty = value[2] as Boolean,
                    isTouched = value[3] as Boolean,
                    isValidated = value[4] as Boolean
                )
            }
        )
    }
}
