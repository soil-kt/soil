// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.serialization.bundle

import androidx.core.bundle.Bundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
@PublishedApi
internal class BundleEncoder(
    private val bundler: Bundler,
    private val parentBundle: Bundle,
    private val parentKey: String? = null
) : AbstractEncoder() {

    private val currentBundle = if (parentKey == null) parentBundle else Bundle()
    private var currentKey: String? = null

    override val serializersModule: SerializersModule = bundler.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val key = currentKey ?: return this
        return BundleEncoder(bundler, currentBundle, key)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        val key = parentKey ?: return
        parentBundle.putBundle(key, currentBundle)
    }

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int
    ): CompositeEncoder {
        val encoder = super.beginCollection(descriptor, collectionSize) as BundleEncoder
        encoder.currentBundle.putInt(Bundler.COLLECTION_SIZE_KEY, collectionSize)
        return encoder
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        currentKey = descriptor.getElementName(index)
        return true
    }

    override fun encodeBoolean(value: Boolean) {
        currentBundle.putBoolean(currentKey, value)
    }

    override fun encodeByte(value: Byte) {
        currentBundle.putByte(currentKey, value)
    }

    override fun encodeChar(value: Char) {
        currentBundle.putChar(currentKey, value)
    }

    override fun encodeDouble(value: Double) {
        currentBundle.putDouble(currentKey, value)
    }

    override fun encodeFloat(value: Float) {
        currentBundle.putFloat(currentKey, value)
    }

    override fun encodeInt(value: Int) {
        currentBundle.putInt(currentKey, value)
    }

    override fun encodeLong(value: Long) {
        currentBundle.putLong(currentKey, value)
    }

    override fun encodeShort(value: Short) {
        currentBundle.putShort(currentKey, value)
    }

    override fun encodeString(value: String) {
        currentBundle.putString(currentKey, value)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeInt(index)
    }

    override fun encodeNull() = Unit
}
