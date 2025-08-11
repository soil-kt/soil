// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.saveable.Saver
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

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
 * @param configuration The SavedState configuration to use for encoding and decoding. Default is [SavedStateConfiguration.DEFAULT].
 * @return The [Saver] for the value.
 */
@ExperimentalSerializationApi
inline fun <reified T : Any> serializationSaver(
    serializer: KSerializer<T> = serializer(),
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT
): Saver<T, Any> {
    return Saver(
        save = { value -> encodeToSavedState(serializer, value, configuration) },
        restore = { savedState -> decodeFromSavedState(serializer, savedState as SavedState, configuration) }
    )
}
