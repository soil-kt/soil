// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormErrors
import soil.form.FormFieldDependencies
import soil.form.FieldNames
import soil.form.FormPolicy
import soil.form.FormRule

/**
 * Remembers a form state for the given initial value.
 *
 * Usage:
 * ```kotlin
 * val formState = rememberFormState(
 *     initialValue = FormData(),
 *     policy = FormPolicy.Minimal,
 *     saver = serializationSaver()
 * )
 * ```
 *
 * @param T The type of the form value.
 * @param initialValue The initial value of the form.
 * @param policy The policy to apply to the form.
 * @param saver The saver to save and restore the form state.
 * @param key The key to reset the form state.
 * @return The remembered [form state][FormState].
 */
@Composable
fun <T : Any> rememberFormState(
    initialValue: T,
    policy: FormPolicy = FormPolicy.Default,
    saver: Saver<T, Any> = autoSaver(),
    key: Any? = null,
): FormState<T> {
    val value = rememberSaveable(key, stateSaver = saver) {
        mutableStateOf(initialValue)
    }
    val errors = rememberSaveable(key, saver = errorsSaver) {
        mutableStateMapOf()
    }
    val triggers = rememberSaveable(key, saver = triggersSaver) {
        mutableStateMapOf()
    }
    val rules = remember(key) {
        mutableStateMapOf<FieldName, FormRule<T>>()
    }
    val dependsOn = remember(key) {
        mutableStateMapOf<FieldName, FieldNames>()
    }
    val watchers = remember(key) {
        // NOTE: Dependency loop detection is not implemented
        derivedStateOf {
            dependsOn.keys.flatMap { key -> dependsOn[key]?.map { Pair(key, it) } ?: emptyList() }
                .groupBy(keySelector = { it.second }, valueTransform = { it.first })
                .mapValues { (_, value) -> value.toSet() }
        }
    }
    val isSubmitting = remember(key) { mutableStateOf(false) }
    val isSubmitted = rememberSaveable(key) { mutableStateOf(false) }
    val submitCount = rememberSaveable(key) { mutableIntStateOf(0) }

    return remember(key) {
        FormState(
            policy = policy,
            initialValue = initialValue,
            value = value,
            isSubmitting = isSubmitting,
            isSubmitted = isSubmitted,
            submitCount = submitCount,
            errors = errors,
            triggers = triggers,
            rules = rules,
            dependsOn = dependsOn,
            watchers = watchers
        )
    }
}

@Suppress("UNCHECKED_CAST")
private val errorsSaver = mapSaver(
    save = { stateMap: SnapshotStateMap<FieldName, FieldErrors> -> stateMap.toMap() },
    restore = { map: Map<String, Any?> ->
        val result = mutableStateMapOf<FieldName, FieldErrors>()
        (map as? Map<FieldName, FieldErrors>)?.forEach { (key, value) ->
            result[key] = value
        }
        result
    }
)

@Suppress("UNCHECKED_CAST")
private val triggersSaver = mapSaver(
    save = { stateMap: SnapshotStateMap<FieldName, FieldValidateOn> -> stateMap.toMap() },
    restore = { map: Map<String, Any?> ->
        val result = mutableStateMapOf<FieldName, FieldValidateOn>()
        (map as? Map<FieldName, FieldValidateOn>)?.forEach { (key, value) ->
            result[key] = value
        }
        result
    }
)

// TODO: TextStateとイメージ合わせるなら単独で使えたほうがVM保持にも対応できる
@Stable
class FormState<T>(
    val policy: FormPolicy,
    val initialValue: T,
    value: MutableState<T>,
    isSubmitting: MutableState<Boolean>,
    isSubmitted: MutableState<Boolean>,
    submitCount: MutableState<Int>,
    errors: SnapshotStateMap<FieldName, FieldErrors>,
    triggers: SnapshotStateMap<FieldName, FieldValidateOn>,
    rules: SnapshotStateMap<FieldName, FormRule<T>>,
    dependsOn: SnapshotStateMap<FieldName, FieldNames>,
    watchers: State<FormFieldDependencies>,
) {
    var value by value
    var isSubmitting: Boolean by isSubmitting
        internal set
    var isSubmitted: Boolean by isSubmitted
        internal set
    var submitCount: Int by submitCount
        internal set
    val errors: MutableMap<FieldName, FieldErrors> = errors
    val triggers: MutableMap<FieldName, FieldValidateOn> = triggers
    val rules: MutableMap<FieldName, FormRule<T>> = rules
    val dependsOn: MutableMap<FieldName, FieldNames> = dependsOn
    val watchers: FormFieldDependencies by watchers

    val hasError: Boolean
        get() = errors.values.any { it.isNotEmpty() }

    internal fun getTriggerFor(field: FieldName): FieldValidateOn {
        return triggers[field] ?: policy.field.validationTrigger.startAt
    }

    internal fun updateError(
        field: FieldName,
        fieldErrors: FieldErrors,
        validateOn: FieldValidateOn = getTriggerFor(field)
    ) {
        errors[field] = fieldErrors
        triggers[field] = policy.field.validationTrigger.next(validateOn, fieldErrors.isEmpty())
    }

    internal fun forceError(errors: FormErrors, validateOn: FieldValidateOn) {
        errors.forEach { (field, fieldErrors) -> updateError(field, fieldErrors, validateOn) }
    }

    internal fun revalidateDependsOn(name: FieldName) {
        watchers[name]?.forEach { field ->
            val hasValidatedOnce = errors.containsKey(field)
            if (hasValidatedOnce) {
                rules[field]?.test(value, dryRun = false)
            }
        }
    }
}
