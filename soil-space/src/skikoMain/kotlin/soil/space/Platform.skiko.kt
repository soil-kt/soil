// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space


actual interface CommonParcelable
actual interface CommonSerializable
actual typealias CommonBundle = FakeBundle

@Suppress("UNUSED_PARAMETER")
class FakeBundle {
    fun containsKey(key: String): Boolean = false
    fun putString(key: String?, value: String?) = Unit
    fun getString(key: String?): String? = null
    fun putBoolean(key: String?, value: Boolean) = Unit
    fun getBoolean(key: String): Boolean = false
    fun putInt(key: String?, value: Int) = Unit
    fun getInt(key: String): Int = 0
    fun putLong(key: String?, value: Long) = Unit
    fun getLong(key: String?): Long = 0
    fun putDouble(key: String?, value: Double) = Unit
    fun getDouble(key: String?): Double = 0.0
    fun putFloat(key: String?, value: Float) = Unit
    fun getFloat(key: String?): Float = 0.0f
    fun putChar(key: String?, value: Char) = Unit
    fun getChar(key: String?): Char = Char.MIN_VALUE
    fun putShort(key: String?, value: Short) = Unit
    fun getShort(key: String?): Short = 0
    fun putByte(key: String?, value: Byte) = Unit
    fun getByte(key: String?): Byte = Byte.MIN_VALUE
    fun putParcelable(key: String?, value: CommonParcelable?) = Unit
    fun <T : CommonParcelable> getParcelable(key: String?): T? = null
    fun putParcelableArray(key: String?, value: Array<out CommonParcelable>?) = Unit
    fun getParcelableArray(key: String?): Array<out CommonParcelable>? = null
    fun putParcelableArrayList(key: String?, value: ArrayList<out CommonParcelable>?) = Unit
    fun <T : CommonParcelable> getParcelableArrayList(key: String?): ArrayList<T>? = null
    fun putSerializable(key: String?, value: CommonSerializable?) = Unit
    fun getSerializable(key: String?): CommonSerializable? = null
    fun putIntegerArrayList(key: String?, value: ArrayList<Int>?) = Unit
    fun getIntegerArrayList(key: String?): ArrayList<Int>? = null
    fun putStringArrayList(key: String?, value: ArrayList<String>?) = Unit
    fun getStringArrayList(key: String?): ArrayList<String>? = null
    fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?) = Unit
    fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>? = null
}

actual fun interface CommonSavedStateProvider {
    actual fun saveState(): CommonBundle
}
