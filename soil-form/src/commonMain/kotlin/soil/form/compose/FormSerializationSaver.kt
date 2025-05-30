// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.saveable.Saver
import androidx.core.bundle.Bundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import soil.serialization.bundle.Bundler

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
