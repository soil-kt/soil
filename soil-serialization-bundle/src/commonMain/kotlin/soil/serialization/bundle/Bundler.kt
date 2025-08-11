// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.serialization.bundle

import androidx.core.bundle.Bundle
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * Implements [encoding][encodeToBundle] and [decoding][decodeFromBundle] classes to/from [androidx.core.bundle.Bundle](https://github.com/JetBrains/compose-multiplatform-core/tree/jb-main/core/core-bundle).
 *
 * Usage:
 *
 * ```
 * @Serializable
 * class Data(val property1: String)
 *
 * @Serializable
 * class DataHolder(val data: Data, val property2: String)
 *
 * val bundle = Bundler.encodeToBundle(DataHolder(Data("value1"), "value2"))
 *
 * val dataHolder = Bundler.decodeFromBundle<DataHolder>(bundle)
 * ```
 *
 * @param serializersModule A [SerializersModule] which should contain registered serializers
 * for [kotlinx.serialization.Contextual] and [kotlinx.serialization.Polymorphic] serialization, if you have any.
 */
@Deprecated(
    message = "Bundler is deprecated. Please use Android's official SavedState serialization instead. " +
        "Use androidx.savedstate.serialization.encodeToSavedState() and decodeFromSavedState() functions. " +
        "For more information, see: https://developer.android.com/jetpack/androidx/releases/savedstate#1.3.0",
    level = DeprecationLevel.WARNING
)
open class Bundler(
    override val serializersModule: SerializersModule
) : SerialFormat {

    /**
     * Encodes the given [value] to a [Bundle] using the provided [serializer] of type [T].
     *
     * @param T The type of the value to encode
     * @param value The value to encode
     * @param serializer The serializer to use
     */
    @ExperimentalSerializationApi
    inline fun <reified T> encodeToBundle(
        value: T,
        serializer: SerializationStrategy<T> = serializer()
    ): Bundle {
        val result = Bundle()
        val encoder = BundleEncoder(this, result)
        encoder.encodeSerializableValue(serializer, value)
        return result
    }

    /**
     * Decodes a value from the given [bundle] using the provided [deserializer] of type [T].
     *
     * @param T The type of the value to decode
     * @param bundle The bundle to decode from
     * @param deserializer The deserializer to use
     */
    @ExperimentalSerializationApi
    inline fun <reified T> decodeFromBundle(
        bundle: Bundle,
        deserializer: DeserializationStrategy<T> = serializer()
    ): T {
        val decoder = BundleDecoder(this, bundle)
        return decoder.decodeSerializableValue(deserializer)
    }

    /**
     * The default instance of Bundler
     */
    companion object : Bundler(serializersModule = EmptySerializersModule()) {
        internal const val COLLECTION_SIZE_KEY = "\$size"
    }
}
