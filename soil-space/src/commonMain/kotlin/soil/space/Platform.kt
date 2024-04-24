// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

expect interface CommonParcelable
expect interface CommonSerializable

expect class CommonBundle() {
    fun containsKey(key: String): Boolean
    fun putString(key: String?, value: String?)
    fun getString(key: String?): String?
    fun putBoolean(key: String?, value: Boolean)
    fun getBoolean(key: String): Boolean
    fun putInt(key: String?, value: Int)
    fun getInt(key: String): Int
    fun putLong(key: String?, value: Long)
    fun getLong(key: String?): Long
    fun putDouble(key: String?, value: Double)
    fun getDouble(key: String?): Double
    fun putFloat(key: String?, value: Float)
    fun getFloat(key: String?): Float
    fun putChar(key: String?, value: Char)
    fun getChar(key: String?): Char
    fun putShort(key: String?, value: Short)
    fun getShort(key: String?): Short
    fun putByte(key: String?, value: Byte)
    fun getByte(key: String?): Byte
    fun putParcelable(key: String?, value: CommonParcelable?)
    fun <T : CommonParcelable> getParcelable(key: String?): T?
    fun putParcelableArray(key: String?, value: Array<out CommonParcelable>?)
    fun getParcelableArray(key: String?): Array<out CommonParcelable>?
    fun putParcelableArrayList(key: String?, value: ArrayList<out CommonParcelable>?)
    fun <T : CommonParcelable> getParcelableArrayList(key: String?): ArrayList<T>?
    fun putSerializable(key: String?, value: CommonSerializable?)
    fun getSerializable(key: String?): CommonSerializable?
    fun putIntegerArrayList(key: String?, value: ArrayList<Int>?)
    fun getIntegerArrayList(key: String?): ArrayList<Int>?
    fun putStringArrayList(key: String?, value: ArrayList<String>?)
    fun getStringArrayList(key: String?): ArrayList<String>?
    fun putCharSequenceArrayList(key: String?, value: ArrayList<CharSequence>?)
    fun getCharSequenceArrayList(key: String?): ArrayList<CharSequence>?
}

expect fun interface CommonSavedStateProvider {
    fun saveState(): CommonBundle
}
