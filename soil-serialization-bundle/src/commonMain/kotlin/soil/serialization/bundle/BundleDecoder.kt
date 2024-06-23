// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.serialization.bundle

import androidx.core.bundle.Bundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
@PublishedApi
internal class BundleDecoder(
    private val bundler: Bundler,
    private val savedBundle: Bundle,
    descriptor: SerialDescriptor? = null
) : AbstractDecoder() {

    private var currentStructure: SerialDescriptor? = descriptor
    private var currentStructureIndex: Int = if (descriptor != null) 0 else -1

    override val serializersModule: SerializersModule = bundler.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (currentStructure == null) {
            currentStructure = descriptor
            currentStructureIndex = 0
            return this
        }
        val nestedBundle = savedBundle.getBundle(extractElementKey())!!
        return BundleDecoder(bundler, nestedBundle, descriptor)
    }

    override fun endStructure(descriptor: SerialDescriptor) = Unit

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        error("Should not be called when decodeSequentially=true")
    }

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return savedBundle.getInt(Bundler.COLLECTION_SIZE_KEY)
    }

    override fun decodeBoolean(): Boolean {
        return savedBundle.getBoolean(extractElementKey())
    }

    override fun decodeByte(): Byte {
        return savedBundle.getByte(extractElementKey())
    }

    override fun decodeChar(): Char {
        return savedBundle.getChar(extractElementKey())
    }

    override fun decodeDouble(): Double {
        return savedBundle.getDouble(extractElementKey())
    }

    override fun decodeFloat(): Float {
        return savedBundle.getFloat(extractElementKey())
    }

    override fun decodeInt(): Int {
        return savedBundle.getInt(extractElementKey())
    }

    override fun decodeLong(): Long {
        return savedBundle.getLong(extractElementKey())
    }

    override fun decodeShort(): Short {
        return savedBundle.getShort(extractElementKey())
    }

    override fun decodeString(): String {
        return savedBundle.getString(extractElementKey())!!
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return decodeInt()
    }

    override fun decodeNotNullMark(): Boolean {
        val key = currentStructure?.getElementName(currentStructureIndex)
        return savedBundle.containsKey(key)
    }

    override fun decodeNull(): Nothing? {
        currentStructureIndex++
        return null
    }

    private fun extractElementKey(): String? {
        return currentStructure?.getElementName(currentStructureIndex++)
    }
}
