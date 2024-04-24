// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmName

@Immutable
class Atom<T>(
    val initialValue: T,
    val saver: AtomSaver<T>? = null,
    val scope: AtomScope? = null
)

fun <T> atom(
    initialValue: T,
    saver: AtomSaver<T>? = null,
    scope: AtomScope? = null
): Atom<T> {
    return Atom(initialValue, saver, scope)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    val saver: AtomSaver<T>? = when (T::class) {
        String::class -> stringSaver(saverKey) as AtomSaver<T>
        Boolean::class -> booleanSaver(saverKey) as AtomSaver<T>
        Int::class -> intSaver(saverKey) as AtomSaver<T>
        Long::class -> longSaver(saverKey) as AtomSaver<T>
        Double::class -> doubleSaver(saverKey) as AtomSaver<T>
        Float::class -> floatSaver(saverKey) as AtomSaver<T>
        Char::class -> charSaver(saverKey) as AtomSaver<T>
        Short::class -> shortSaver(saverKey) as AtomSaver<T>
        Byte::class -> byteSaver(saverKey) as AtomSaver<T>
        else -> null
    }
    return atom(initialValue, saver, scope)
}

inline fun <reified T : CommonParcelable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, parcelableSaver(saverKey), scope)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> atom(
    initialValue: ArrayList<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ArrayList<T>> {
    val saver = when (T::class) {
        String::class -> stringArrayListSaver(saverKey) as AtomSaver<ArrayList<T>>
        Int::class -> integerArrayListSaver(saverKey) as AtomSaver<ArrayList<T>>
        CharSequence::class -> charSequenceArrayListSaver(saverKey) as AtomSaver<ArrayList<T>>
        else -> null
    }
    return atom(initialValue, saver, scope)
}

@JvmName("atomWithParcelable")
inline fun <reified T : CommonParcelable> atom(
    initialValue: ArrayList<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ArrayList<T>> {
    return atom(initialValue, parcelableArrayListSaver(saverKey), scope)
}

inline fun <reified T : CommonParcelable> atom(
    initialValue: Array<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<Array<T>> {
    return atom(initialValue, parcelableArraySaver(saverKey), scope)
}

inline fun <reified T : CommonSerializable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, serializableSaver(saverKey), scope)
}

interface AtomSaver<T> {
    fun save(bundle: CommonBundle, value: T)
    fun restore(bundle: CommonBundle): T?
}

typealias AtomSaverKey = String

fun stringSaver(key: AtomSaverKey): AtomSaver<String> {
    return object : AtomSaver<String> {
        override fun save(bundle: CommonBundle, value: String) {
            bundle.putString(key, value)
        }

        override fun restore(bundle: CommonBundle): String? {
            return if (bundle.containsKey(key)) bundle.getString(key) else null
        }
    }
}

fun booleanSaver(key: AtomSaverKey): AtomSaver<Boolean> {
    return object : AtomSaver<Boolean> {
        override fun save(bundle: CommonBundle, value: Boolean) {
            bundle.putBoolean(key, value)
        }

        override fun restore(bundle: CommonBundle): Boolean? {
            return if (bundle.containsKey(key)) bundle.getBoolean(key) else null
        }
    }
}

fun intSaver(key: AtomSaverKey): AtomSaver<Int> {
    return object : AtomSaver<Int> {
        override fun save(bundle: CommonBundle, value: Int) {
            bundle.putInt(key, value)
        }

        override fun restore(bundle: CommonBundle): Int? {
            return if (bundle.containsKey(key)) bundle.getInt(key) else null
        }
    }
}

fun longSaver(key: AtomSaverKey): AtomSaver<Long> {
    return object : AtomSaver<Long> {
        override fun save(bundle: CommonBundle, value: Long) {
            bundle.putLong(key, value)
        }

        override fun restore(bundle: CommonBundle): Long? {
            return if (bundle.containsKey(key)) bundle.getLong(key) else null
        }
    }
}

fun doubleSaver(key: AtomSaverKey): AtomSaver<Double> {
    return object : AtomSaver<Double> {
        override fun save(bundle: CommonBundle, value: Double) {
            bundle.putDouble(key, value)
        }

        override fun restore(bundle: CommonBundle): Double? {
            return if (bundle.containsKey(key)) bundle.getDouble(key) else null
        }
    }
}

fun floatSaver(key: AtomSaverKey): AtomSaver<Float> {
    return object : AtomSaver<Float> {
        override fun save(bundle: CommonBundle, value: Float) {
            bundle.putFloat(key, value)
        }

        override fun restore(bundle: CommonBundle): Float? {
            return if (bundle.containsKey(key)) bundle.getFloat(key) else null
        }
    }
}

fun charSaver(key: AtomSaverKey): AtomSaver<Char> {
    return object : AtomSaver<Char> {
        override fun save(bundle: CommonBundle, value: Char) {
            bundle.putChar(key, value)
        }

        override fun restore(bundle: CommonBundle): Char? {
            return if (bundle.containsKey(key)) bundle.getChar(key) else null
        }
    }
}

fun shortSaver(key: AtomSaverKey): AtomSaver<Short> {
    return object : AtomSaver<Short> {
        override fun save(bundle: CommonBundle, value: Short) {
            bundle.putShort(key, value)
        }

        override fun restore(bundle: CommonBundle): Short? {
            return if (bundle.containsKey(key)) bundle.getShort(key) else null
        }
    }
}

fun byteSaver(key: AtomSaverKey): AtomSaver<Byte> {
    return object : AtomSaver<Byte> {
        override fun save(bundle: CommonBundle, value: Byte) {
            bundle.putByte(key, value)
        }

        override fun restore(bundle: CommonBundle): Byte? {
            return if (bundle.containsKey(key)) bundle.getByte(key) else null
        }
    }
}

expect inline fun <reified T : CommonParcelable> parcelableSaver(key: AtomSaverKey): AtomSaver<T>

expect inline fun <reified T : CommonParcelable> parcelableArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<T>>

expect inline fun <reified T : CommonParcelable> parcelableArraySaver(key: AtomSaverKey): AtomSaver<Array<T>>

expect inline fun <reified T : CommonSerializable> serializableSaver(key: AtomSaverKey): AtomSaver<T>

fun integerArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<Int>> {
    return object : AtomSaver<ArrayList<Int>> {
        override fun save(bundle: CommonBundle, value: ArrayList<Int>) {
            bundle.putIntegerArrayList(key, value)
        }

        override fun restore(bundle: CommonBundle): ArrayList<Int>? {
            return bundle.getIntegerArrayList(key)
        }
    }
}

fun stringArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<String>> {
    return object : AtomSaver<ArrayList<String>> {
        override fun save(bundle: CommonBundle, value: ArrayList<String>) {
            bundle.putStringArrayList(key, value)
        }

        override fun restore(bundle: CommonBundle): ArrayList<String>? {
            return bundle.getStringArrayList(key)
        }
    }
}

fun charSequenceArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<CharSequence>> {
    return object : AtomSaver<ArrayList<CharSequence>> {
        override fun save(bundle: CommonBundle, value: ArrayList<CharSequence>) {
            bundle.putCharSequenceArrayList(key, value)
        }

        override fun restore(bundle: CommonBundle): ArrayList<CharSequence>? {
            return bundle.getCharSequenceArrayList(key)
        }
    }
}

class AtomScope

fun atomScope() = AtomScope()
