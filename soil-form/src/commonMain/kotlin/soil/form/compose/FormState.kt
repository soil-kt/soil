// Copyright 2024 Soil Contributors
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
import soil.form.FieldErrors
import soil.form.FieldMeta
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormData
import soil.form.FormMeta
import soil.form.FormPolicy
import soil.form.noErrors

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
fun <T : Any> rememberFormState(
    initialValue: T,
    saver: Saver<T, Any> = autoSaver(),
    policy: FormPolicy = FormPolicy.Default
): FormState<T> {
    return rememberSaveable(saver = FormState.Saver(formSaver = saver, formPolicy = policy)) {
        formStateOf(value = initialValue, policy = policy)
    }
}

@Composable
fun <T : Any> rememberFormState(
    initialValue: T,
    initialMeta: FormMetaState<T>,
    saver: Saver<T, Any> = autoSaver(),
    policy: FormPolicy = FormPolicy.Default
): FormState<T> {
    return rememberSaveable(saver = FormState.Saver(formSaver = saver, formPolicy = policy)) {
        formStateOf(value = initialValue, meta = initialMeta, policy = policy)
    }
}

fun <T : Any> formStateOf(
    value: T,
    meta: FormMetaState<T> = formMetaStateOf(defaultValue = value),
    policy: FormPolicy = FormPolicy.Default
): FormState<T> = FormState(
    value = value,
    meta = meta,
    policy = policy
)

@Stable
class FormState<T : Any> internal constructor(
    value: T,
    override val meta: FormMetaState<T>,
    val policy: FormPolicy
) : FormData<T> {

    override var value: T by mutableStateOf(value)

    fun reset(newValue: T = meta.defaultValue) {
        Snapshot.withMutableSnapshot {
            value = newValue
            meta.defaultValue = newValue
            meta.fields.forEach { (_, fieldMeta) ->
                fieldMeta.errors = noErrors
                fieldMeta.trigger = policy.field.validationTrigger.startAt
                fieldMeta.isDirty = false
                fieldMeta.isTouched = false
                fieldMeta.hasBeenValidated = false
            }
        }
    }

    fun setError(vararg pairs: Pair<FieldName, FieldErrors>) {
        Snapshot.withMutableSnapshot {
            pairs.forEach { (fieldName, fieldErrors) ->
                meta.fields[fieldName]?.let { fieldMeta ->
                    fieldMeta.errors = fieldErrors + fieldMeta.errors
                }
            }
        }
    }

    companion object {
        fun <T : Any> Saver(formSaver: Saver<T, Any>, formPolicy: FormPolicy) = Saver<FormState<T>, Any>(
            save = { value ->
                listOf(
                    with(formSaver) {
                        save(value.value)
                    },
                    with(FormMetaState.Saver(formSaver)) {
                        save(value.meta)
                    }
                )
            },
            restore = {
                val (value, meta) = it as List<*>
                FormState(
                    value = with(formSaver) {
                        restore(checkNotNull(value)) as T
                    },
                    meta = with(FormMetaState.Saver(formSaver)) {
                        restore(checkNotNull(meta)) as FormMetaState<T>
                    },
                    policy = formPolicy
                )
            }
        )
    }
}

fun <T : Any> formMetaStateOf(
    fields: Map<FieldName, FieldMetaState> = emptyMap(),
    defaultValue: T
): FormMetaState<T> = FormMetaState(
    fields = fields,
    defaultValue = defaultValue
)

class FormMetaState<T : Any> internal constructor(
    fields: Map<FieldName, FieldMetaState>,
    defaultValue: T
) : FormMeta<T> {

    override val fields: MutableMap<FieldName, FieldMetaState> =
        mutableMapOf(*fields.map { (k, v) -> k to v }.toTypedArray())

    override var defaultValue: T by mutableStateOf(defaultValue)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> Saver(formSaver: Saver<T, Any>) = Saver<FormMetaState<T>, Any>(
            save = { value ->
                listOf(
                    value.fields.mapValues {
                        with(FieldMetaState.Saver()) {
                            save(it.value)
                        }
                    },
                    with(formSaver) {
                        save(value.defaultValue)
                    }
                )
            },
            restore = {
                val (fields, defaultValue) = it as List<*>
                FormMetaState(
                    fields = with(FieldMetaState.Saver()) {
                        fields as Map<FieldName, Any>
                        fields.mapValues { (_, v) ->
                            restore(v) as FieldMetaState
                        }
                    },
                    defaultValue = with(formSaver) {
                        restore(checkNotNull(defaultValue)) as T
                    }
                )
            }
        )
    }
}

fun fieldMetaStateOf(
    errors: FieldErrors = noErrors,
    trigger: FieldValidateOn = FieldValidateOn.Blur,
    isDirty: Boolean = false,
    isTouched: Boolean = false,
    hasBeenValidated: Boolean = false
): FieldMetaState = FieldMetaState(
    errors = errors,
    trigger = trigger,
    isDirty = isDirty,
    isTouched = isTouched,
    hasBeenValidated = hasBeenValidated
)

class FieldMetaState internal constructor(
    errors: FieldErrors,
    trigger: FieldValidateOn,
    isDirty: Boolean,
    isTouched: Boolean,
    hasBeenValidated: Boolean
) : FieldMeta {

    override var errors: FieldErrors by mutableStateOf(errors)
    override var trigger: FieldValidateOn by mutableStateOf(trigger)
    override var isDirty: Boolean by mutableStateOf(isDirty)
    override var isTouched: Boolean by mutableStateOf(isTouched)
    override var hasBeenValidated: Boolean by mutableStateOf(hasBeenValidated)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun Saver() = Saver<FieldMetaState, Any>(
            save = { value ->
                listOf(
                    value.errors,
                    value.trigger,
                    value.isDirty,
                    value.isTouched,
                    value.hasBeenValidated
                )
            },
            restore = {
                val (errors, trigger, isDirty, isTouched, hasBeenValidated) = it as List<*>
                FieldMetaState(
                    errors = errors as FieldErrors,
                    trigger = trigger as FieldValidateOn,
                    isDirty = isDirty as Boolean,
                    isTouched = isTouched as Boolean,
                    hasBeenValidated = hasBeenValidated as Boolean
                )
            }
        )
    }
}
