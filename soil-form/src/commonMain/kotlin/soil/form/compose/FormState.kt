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

@Stable
class FormState<T> internal constructor(
    value: T,
    override val meta: FormMetaState,
    val policy: FormPolicy
) : FormData<T> {

    constructor(value: T, policy: FormPolicy = FormPolicy()) : this(
        value = value,
        meta = FormMetaState(canSubmit = !policy.formOptions.preValidation),
        policy = policy
    )

    override var value: T by mutableStateOf(value)

    fun reset(newValue: T) {
        Snapshot.withMutableSnapshot {
            value = newValue
            meta.fields.forEach { (_, fieldMeta) ->
                fieldMeta.error = noFieldError
                fieldMeta.mode = policy.fieldOptions.validationStrategy.initial
                fieldMeta.isDirty = false
                fieldMeta.isTouched = false
                fieldMeta.isValidated = false
            }
            meta.canSubmit = !policy.formOptions.preValidation
        }
    }

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
        return "FormState(value=$value, meta=$meta, policy=$policy)"
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
                    }
                )
            },
            restore = {
                val (value, meta) = it as List<*>
                FormState(
                    value = with(formSaver) {
                        @Suppress("UNCHECKED_CAST")
                        restore(checkNotNull(value)) as T
                    },
                    meta = with(FormMetaState.Saver()) {
                        restore(checkNotNull(meta)) as FormMetaState
                    },
                    policy = formPolicy
                )
            }
        )
    }
}

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
