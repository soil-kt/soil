// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormFieldNames
import soil.form.FormPolicy
import soil.form.FormRule
import soil.form.FormValidationException

@Composable
fun <T : Any> Form(
    onSubmit: suspend (T) -> Unit,
    initialValue: T,
    modifier: Modifier = Modifier,
    onError: ((err: Throwable) -> Unit)? = null,
    saver: Saver<T, Any> = autoSaver(),
    key: Any? = null,
    policy: FormPolicy = FormPolicy.Default,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable FormScope<T>.() -> Unit
) {
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
        mutableStateMapOf<FieldName, FormFieldNames>()
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
    val state = remember(key) {
        FormStateImpl(
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
    val submitHandler = remember(key) {
        SubmitHandler { rule ->
            if (state.isSubmitting) {
                return@SubmitHandler
            }
            val formValue = state.value
            state.isSubmitting = true
            coroutineScope.launch {
                var isCompleted = false
                try {
                    if (rule.test(formValue, dryRun = false)) {
                        onSubmit(formValue)
                        isCompleted = true
                    }
                } catch (e: FormValidationException) {
                    state.forceError(e.errors, FieldValidateOn.Submit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    onError?.invoke(e)
                } finally {
                    state.isSubmitting = false
                    state.isSubmitted = isCompleted
                    state.submitCount += 1
                }
            }
        }
    }
    val formScope = remember(key) {
        FormScope(
            state = state,
            submitHandler = submitHandler
        )
    }
    Box(modifier = modifier) {
        with(formScope) { content() }
    }
}

@Suppress("UNCHECKED_CAST")
private val errorsSaver = mapSaver(
    save = { stateMap -> stateMap.toMap() },
    restore = { (it as? Map<FieldName, FieldErrors>)?.toList()?.toMutableStateMap() }
)

@Suppress("UNCHECKED_CAST")
private val triggersSaver = mapSaver(
    save = { stateMap -> stateMap.toMap() },
    restore = { (it as? Map<FieldName, FieldValidateOn>)?.toList()?.toMutableStateMap() }
)
