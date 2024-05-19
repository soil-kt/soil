// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmName

/**
 * Atom is a unit of state in Space.
 *
 * The instance created serves as a key to store values of a specified type in [AtomStore].
 * It is designed so that the instance itself acts as the reference key, instead of the user having to assign a unique key.
 * Therefore, even if the definitions are exactly the same, different instances will be managed as separate keys.
 *
 * **Note:**
 * Specifying a [saver] is optional, but if you expect state restoration on the Android platform, it is crucial to specify it.
 *
 * Usage:
 *
 * ```kotlin
 * val counterAtom = atom(0)
 * val titleAtom = atom("hello, world", saverKey = "title")
 * ```
 *
 * @param T The type of the value to be stored.
 * @property initialValue The initial value to be stored.
 * @property saver The saver to be used to save and restore the value.
 * @property scope The scope to be used to manage the value.
 */
@Immutable
class Atom<T> internal constructor(
    val initialValue: T,
    val saver: AtomSaver<T>? = null,
    val scope: AtomScope? = null
)

/**
 * Creates an [Atom].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saver The saver to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
fun <T> atom(
    initialValue: T,
    saver: AtomSaver<T>? = null,
    scope: AtomScope? = null
): Atom<T> {
    return Atom(initialValue, saver, scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey].
 *
 * Automatically selects an [AtomSaver] for the [Type][T] if it matches any of the following.
 * The [saverKey] is used as a key for the [AtomSaver].
 *
 *    | Type        | AtomSaver     |
 *    | :---------- | :-------------|
 *    | String      | stringSaver   |
 *    | Boolean     | booleanSaver  |
 *    | Int         | intSaver      |
 *    | Long        | longSaver     |
 *    | Double      | doubleSaver   |
 *    | Float       | floatSaver    |
 *    | Char        | charSaver     |
 *    | Short       | shortSaver    |
 *    | Byte        | byteSaver     |
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
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

/**
 * Creates an [Atom] using [AtomSaverKey] for [CommonParcelable].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
inline fun <reified T : CommonParcelable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, parcelableSaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [ArrayList].
 *
 * Automatically selects an [AtomSaver] for the [Type][T] if it matches any of the following.
 * The [saverKey] is used as a key for the [AtomSaver].
 *
 *    | Type            | AtomSaver                     |
 *    | :-------------- | :---------------------------- |
 *    | String          | stringArrayListSaver          |
 *    | Int             | integerArrayListSaver         |
 *    | CharSequence    | charSequenceArrayListSaver    |
 *
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
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

/**
 * Creates an [Atom] using [AtomSaverKey] for [ArrayList] with [CommonParcelable][T].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithParcelable")
inline fun <reified T : CommonParcelable> atom(
    initialValue: ArrayList<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ArrayList<T>> {
    return atom(initialValue, parcelableArrayListSaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [Array] with [CommonParcelable][T].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
inline fun <reified T : CommonParcelable> atom(
    initialValue: Array<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<Array<T>> {
    return atom(initialValue, parcelableArraySaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [CommonSerializable].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
inline fun <reified T : CommonSerializable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, serializableSaver(saverKey), scope)
}

/**
 * Interface for saving and restoring values to a [CommonBundle].
 *
 * Currently, this restoration feature is designed specifically for the Android Platform.
 *
 * @param T The type of the value to be saved and restored.
 */
interface AtomSaver<T> {

    /**
     * Saves a value to a [CommonBundle].
     *
     * @param bundle The [CommonBundle] to save the value to.
     * @param value The value to save.
     */
    fun save(bundle: CommonBundle, value: T)

    /**
     * Restores a value from a [CommonBundle].
     *
     * @param bundle The [CommonBundle] to restore the value from.
     * @return The restored value.
     */
    fun restore(bundle: CommonBundle): T?
}

typealias AtomSaverKey = String

// TODO Switch to internal using @PublishedApi
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

/**
 * Provides a scope for creating [Atom]s that manage state in different [AtomStore]s.
 *
 * Similar to [Atom], each instance acts as a key representing a single scope.
 *
 * Usage:
 *
 * ```kotlin
 * val navGraphScope = atomScope()
 * val screenScope = atomScope()
 *
 * val counter1Atom = atom(0, scope = navGraphScope)
 * val counter2Atom = atom(0, scope = screenScope)
 * ```
 */
class AtomScope internal constructor()

/**
 * Creates an [AtomScope].
 */
fun atomScope() = AtomScope()
