// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.ui.Modifier
import androidx.core.bundle.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import soil.form.FormPolicy
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
    val formScope = rememberForm(
        initialValue = initialValue,
        policy = policy,
        saver = saver,
        key = key,
        coroutineScope = coroutineScope,
        onError = onError,
        onSubmit = onSubmit
    )
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
