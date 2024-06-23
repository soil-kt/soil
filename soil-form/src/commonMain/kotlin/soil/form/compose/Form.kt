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
import androidx.core.bundle.Bundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldValidateOn
import soil.form.FormFieldNames
import soil.form.FormPolicy
import soil.form.FormRule
import soil.form.FormValidationException
import soil.serialization.bundle.Bundler

/**
 * A Form to manage the state and actions of input fields, and create a child block of [FormScope].
 *
 * Usage:
 * ```kotlin
 * Form(
 *     onSubmit = {
 *         // Handle submit
 *     },
 *     initialValue = "",
 *     policy = FormPolicy.Minimal
 * ) { // this: FormScope<String>
 *   ..
 * }
 * ```
 *
 * **Note:**
 * If you are expecting state restoration on the Android platform, please check if the type specified in [initialValue] is restorable.
 * Inside the Form, `rememberSaveable` is used to manage input values, and runtime errors will be thrown from the API for unsupported types.
 *
 * @param T The type of the form value.
 * @param onSubmit The submit handler to call when the form is submit.
 * @param initialValue The initial value of the form.
 * @param modifier The modifier to apply to this layout node.
 * @param onError The error handler to call when an error occurs on submit.
 * @param saver The saver to save and restore the form state.
 * @param key The key to reset the form state.
 * @param policy The policy to apply to the form.
 * @param coroutineScope The coroutine scope to launch the submit handler.
 * @param content The content block to create the child block of [FormScope].
 */
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

/**
 * Create an [Saver] for Kotlin Serialization.
 *
 * Usage:
 *
 * ```kotlin
 * @Serializable
 * data class FormData(
 *     val firstName: String = "",
 *     val lastName: String = "",
 *     val email: String = "",
 *     val mobileNumber: String = "",
 *     val title: Title? = null,
 *     val developer: Boolean? = null
 * )
 *
 * enum class Title {
 *     Mr,
 *     Mrs,
 *     Miss,
 *     Dr,
 * }
 *
 * Form(
 *     onSubmit = { .. },
 *     initialValue = FormData(),
 *     modifier = modifier,
 *     saver = serializationSaver()
 * ) { .. }
 * ```
 *
 * @param T The type of the value to save and restore.
 * @param serializer The serializer to use for the value.
 * @param bundler The bundler to encode and decode the value. Default is [Bundler].
 * @return The [Saver] for the value.
 */
@ExperimentalSerializationApi
inline fun <reified T> serializationSaver(
    serializer: KSerializer<T> = serializer(),
    bundler: Bundler = Bundler
): Saver<T, Any> {
    return Saver(
        save = { value -> bundler.encodeToBundle(value, serializer) },
        restore = { bundle -> bundler.decodeFromBundle(bundle as Bundle, serializer) }
    )
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
