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
import kotlinx.coroutines.launch
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FormData
import soil.form.FormOptions
import soil.form.annotation.InternalSoilFormApi

@Stable
interface Form<T> : HasFormBinding<T> {
    val state: FormData<T>

    fun handleSubmit()
}

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

@OptIn(FlowPreview::class)
@Composable
fun <T> rememberForm(
    state: FormState<T>,
    onSubmit: (T) -> Unit
): Form<T> {
    val control = remember(state) {
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
