// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
package soil.space

import androidx.compose.runtime.Immutable
import androidx.core.bundle.Bundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import soil.serialization.bundle.Bundler
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
        Boolean::class -> booleanSaver(saverKey) as AtomSaver<T>
        Byte::class -> byteSaver(saverKey) as AtomSaver<T>
        Char::class -> charSaver(saverKey) as AtomSaver<T>
        Short::class -> shortSaver(saverKey) as AtomSaver<T>
        Int::class -> intSaver(saverKey) as AtomSaver<T>
        Long::class -> longSaver(saverKey) as AtomSaver<T>
        Float::class -> floatSaver(saverKey) as AtomSaver<T>
        Double::class -> doubleSaver(saverKey) as AtomSaver<T>
        String::class -> stringSaver(saverKey) as AtomSaver<T>
        CharSequence::class -> charSequenceSaver(saverKey) as AtomSaver<T>
        else -> null
    }
    return atom(initialValue, saver, scope)
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
@JvmName("atomWithArrayList")
inline fun <reified T> atom(
    initialValue: ArrayList<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ArrayList<T>> {
    val saver = when (T::class) {
        Int::class -> integerArrayListSaver(saverKey) as AtomSaver<ArrayList<T>>
        String::class -> stringArrayListSaver(saverKey) as AtomSaver<ArrayList<T>>
        else -> null
    }
    return atom(initialValue, saver, scope)
}

@JvmName("atomWithBooleanArray")
inline fun atom(
    initialValue: BooleanArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<BooleanArray> {
    return atom(initialValue, booleanArraySaver(saverKey), scope)
}

@JvmName("atomWithByteArray")
inline fun atom(
    initialValue: ByteArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ByteArray> {
    return atom(initialValue, byteArraySaver(saverKey), scope)
}

@JvmName("atomWithShortArray")
inline fun atom(
    initialValue: ShortArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ShortArray> {
    return atom(initialValue, shortArraySaver(saverKey), scope)
}

@JvmName("atomWithCharArray")
inline fun atom(
    initialValue: CharArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<CharArray> {
    return atom(initialValue, charArraySaver(saverKey), scope)
}

@JvmName("atomWithIntArray")
inline fun atom(
    initialValue: IntArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<IntArray> {
    return atom(initialValue, intArraySaver(saverKey), scope)
}

@JvmName("atomWithLongArray")
inline fun atom(
    initialValue: LongArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<LongArray> {
    return atom(initialValue, longArraySaver(saverKey), scope)
}

@JvmName("atomWithFloatArray")
inline fun atom(
    initialValue: FloatArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<FloatArray> {
    return atom(initialValue, floatArraySaver(saverKey), scope)
}

@JvmName("atomWithDoubleArray")
inline fun atom(
    initialValue: DoubleArray,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<DoubleArray> {
    return atom(initialValue, doubleArraySaver(saverKey), scope)
}

@Suppress("UNCHECKED_CAST")
@JvmName("atomWithArray")
inline fun <reified T> atom(
    initialValue: Array<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<Array<T>> {
    val saver = when (T::class) {
        String::class -> stringArraySaver(saverKey) as AtomSaver<Array<T>>
        CharSequence::class -> charSequenceArraySaver(saverKey) as AtomSaver<Array<T>>
        else -> null
    }
    return atom(initialValue, saver, scope)
}

@JvmName("atomWithBundle")
inline fun atom(
    initialValue: Bundle,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<Bundle> {
    return atom(initialValue, bundleSaver(saverKey), scope)
}


/**
 * Interface for saving and restoring values to a [Bundle].
 *
 * Currently, this restoration feature is designed specifically for the Android Platform.
 *
 * @param T The type of the value to be saved and restored.
 */
interface AtomSaver<T> {

    /**
     * Saves a value to a [Bundle].
     *
     * @param bundle The [Bundle] to save the value to.
     * @param value The value to save.
     */
    fun save(bundle: Bundle, value: T)

    /**
     * Restores a value from a [Bundle].
     *
     * @param bundle The [Bundle] to restore the value from.
     * @return The restored value.
     */
    fun restore(bundle: Bundle): T?
}

typealias AtomSaverKey = String

/**
 * Create an [AtomSaver] for Kotlin Serialization.
 *
 * Usage:
 *
 * ```kotlin
 * @Serializable
 * data class CounterData(
 *     val value: Int = 0
 * )
 *
 * @OptIn(ExperimentalSerializationApi::class)
 * private val counterAtom = atom(CounterData(), saver = serializationSaver("counter"))
 *```
 *
 * @param T The type of the value to save and restore.
 * @param key The key to be used to save and restore the value.
 * @param serializer The serializer to use for the value.
 * @param bundler The bundler to encode and decode the value. Default is [Bundler].
 * @return The [AtomSaver] for the value.
 */
@ExperimentalSerializationApi
inline fun <reified T> serializationSaver(
    key: AtomSaverKey,
    serializer: KSerializer<T> = serializer(),
    bundler: Bundler = Bundler
): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: Bundle, value: T) {
            bundle.putBundle(key, bundler.encodeToBundle(value, serializer))
        }

        override fun restore(bundle: Bundle): T? {
            return bundle.getBundle(key)?.let { bundler.decodeFromBundle(it, serializer) }
        }
    }
}

@PublishedApi
internal fun stringSaver(key: AtomSaverKey): AtomSaver<String> {
    return object : AtomSaver<String> {
        override fun save(bundle: Bundle, value: String) {
            bundle.putString(key, value)
        }

        override fun restore(bundle: Bundle): String? {
            return if (bundle.containsKey(key)) bundle.getString(key) else null
        }
    }
}

@PublishedApi
internal fun charSequenceSaver(key: AtomSaverKey): AtomSaver<CharSequence> {
    return object : AtomSaver<CharSequence> {
        override fun save(bundle: Bundle, value: CharSequence) {
            bundle.putCharSequence(key, value)
        }

        override fun restore(bundle: Bundle): CharSequence? {
            return if (bundle.containsKey(key)) bundle.getCharSequence(key) else null
        }
    }
}

@PublishedApi
internal fun booleanSaver(key: AtomSaverKey): AtomSaver<Boolean> {
    return object : AtomSaver<Boolean> {
        override fun save(bundle: Bundle, value: Boolean) {
            bundle.putBoolean(key, value)
        }

        override fun restore(bundle: Bundle): Boolean? {
            return if (bundle.containsKey(key)) bundle.getBoolean(key) else null
        }
    }
}

@PublishedApi
internal fun intSaver(key: AtomSaverKey): AtomSaver<Int> {
    return object : AtomSaver<Int> {
        override fun save(bundle: Bundle, value: Int) {
            bundle.putInt(key, value)
        }

        override fun restore(bundle: Bundle): Int? {
            return if (bundle.containsKey(key)) bundle.getInt(key) else null
        }
    }
}

@PublishedApi
internal fun longSaver(key: AtomSaverKey): AtomSaver<Long> {
    return object : AtomSaver<Long> {
        override fun save(bundle: Bundle, value: Long) {
            bundle.putLong(key, value)
        }

        override fun restore(bundle: Bundle): Long? {
            return if (bundle.containsKey(key)) bundle.getLong(key) else null
        }
    }
}

@PublishedApi
internal fun doubleSaver(key: AtomSaverKey): AtomSaver<Double> {
    return object : AtomSaver<Double> {
        override fun save(bundle: Bundle, value: Double) {
            bundle.putDouble(key, value)
        }

        override fun restore(bundle: Bundle): Double? {
            return if (bundle.containsKey(key)) bundle.getDouble(key) else null
        }
    }
}

@PublishedApi
internal fun floatSaver(key: AtomSaverKey): AtomSaver<Float> {
    return object : AtomSaver<Float> {
        override fun save(bundle: Bundle, value: Float) {
            bundle.putFloat(key, value)
        }

        override fun restore(bundle: Bundle): Float? {
            return if (bundle.containsKey(key)) bundle.getFloat(key) else null
        }
    }
}

@PublishedApi
internal fun charSaver(key: AtomSaverKey): AtomSaver<Char> {
    return object : AtomSaver<Char> {
        override fun save(bundle: Bundle, value: Char) {
            bundle.putChar(key, value)
        }

        override fun restore(bundle: Bundle): Char? {
            return if (bundle.containsKey(key)) bundle.getChar(key) else null
        }
    }
}

@PublishedApi
internal fun shortSaver(key: AtomSaverKey): AtomSaver<Short> {
    return object : AtomSaver<Short> {
        override fun save(bundle: Bundle, value: Short) {
            bundle.putShort(key, value)
        }

        override fun restore(bundle: Bundle): Short? {
            return if (bundle.containsKey(key)) bundle.getShort(key) else null
        }
    }
}

@PublishedApi
internal fun byteSaver(key: AtomSaverKey): AtomSaver<Byte> {
    return object : AtomSaver<Byte> {
        override fun save(bundle: Bundle, value: Byte) {
            bundle.putByte(key, value)
        }

        override fun restore(bundle: Bundle): Byte? {
            return if (bundle.containsKey(key)) bundle.getByte(key) else null
        }
    }
}

@PublishedApi
internal fun integerArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<Int>> {
    return object : AtomSaver<ArrayList<Int>> {
        override fun save(bundle: Bundle, value: ArrayList<Int>) {
            bundle.putIntegerArrayList(key, value)
        }

        override fun restore(bundle: Bundle): ArrayList<Int>? {
            return bundle.getIntegerArrayList(key)
        }
    }
}

@PublishedApi
internal fun stringArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<String>> {
    return object : AtomSaver<ArrayList<String>> {
        override fun save(bundle: Bundle, value: ArrayList<String>) {
            bundle.putStringArrayList(key, value)
        }

        override fun restore(bundle: Bundle): ArrayList<String>? {
            return bundle.getStringArrayList(key)
        }
    }
}

@PublishedApi
internal fun booleanArraySaver(key: AtomSaverKey): AtomSaver<BooleanArray> {
    return object : AtomSaver<BooleanArray> {
        override fun save(bundle: Bundle, value: BooleanArray) {
            bundle.putBooleanArray(key, value)
        }

        override fun restore(bundle: Bundle): BooleanArray? {
            return bundle.getBooleanArray(key)
        }
    }
}

@PublishedApi
internal fun byteArraySaver(key: AtomSaverKey): AtomSaver<ByteArray> {
    return object : AtomSaver<ByteArray> {
        override fun save(bundle: Bundle, value: ByteArray) {
            bundle.putByteArray(key, value)
        }

        override fun restore(bundle: Bundle): ByteArray? {
            return bundle.getByteArray(key)
        }
    }
}

@PublishedApi
internal fun shortArraySaver(key: AtomSaverKey): AtomSaver<ShortArray> {
    return object : AtomSaver<ShortArray> {
        override fun save(bundle: Bundle, value: ShortArray) {
            bundle.putShortArray(key, value)
        }

        override fun restore(bundle: Bundle): ShortArray? {
            return bundle.getShortArray(key)
        }
    }
}

@PublishedApi
internal fun charArraySaver(key: AtomSaverKey): AtomSaver<CharArray> {
    return object : AtomSaver<CharArray> {
        override fun save(bundle: Bundle, value: CharArray) {
            bundle.putCharArray(key, value)
        }

        override fun restore(bundle: Bundle): CharArray? {
            return bundle.getCharArray(key)
        }
    }
}

@PublishedApi
internal fun intArraySaver(key: AtomSaverKey): AtomSaver<IntArray> {
    return object : AtomSaver<IntArray> {
        override fun save(bundle: Bundle, value: IntArray) {
            bundle.putIntArray(key, value)
        }

        override fun restore(bundle: Bundle): IntArray? {
            return bundle.getIntArray(key)
        }
    }
}

@PublishedApi
internal fun longArraySaver(key: AtomSaverKey): AtomSaver<LongArray> {
    return object : AtomSaver<LongArray> {
        override fun save(bundle: Bundle, value: LongArray) {
            bundle.putLongArray(key, value)
        }

        override fun restore(bundle: Bundle): LongArray? {
            return bundle.getLongArray(key)
        }
    }
}

@PublishedApi
internal fun floatArraySaver(key: AtomSaverKey): AtomSaver<FloatArray> {
    return object : AtomSaver<FloatArray> {
        override fun save(bundle: Bundle, value: FloatArray) {
            bundle.putFloatArray(key, value)
        }

        override fun restore(bundle: Bundle): FloatArray? {
            return bundle.getFloatArray(key)
        }
    }
}

@PublishedApi
internal fun doubleArraySaver(key: AtomSaverKey): AtomSaver<DoubleArray> {
    return object : AtomSaver<DoubleArray> {
        override fun save(bundle: Bundle, value: DoubleArray) {
            bundle.putDoubleArray(key, value)
        }

        override fun restore(bundle: Bundle): DoubleArray? {
            return bundle.getDoubleArray(key)
        }
    }
}

@PublishedApi
internal fun stringArraySaver(key: AtomSaverKey): AtomSaver<Array<String>> {
    return object : AtomSaver<Array<String>> {
        override fun save(bundle: Bundle, value: Array<String>) {
            bundle.putStringArray(key, value)
        }

        override fun restore(bundle: Bundle): Array<String>? {
            return bundle.getStringArray(key)
        }
    }
}

@PublishedApi
internal fun charSequenceArraySaver(key: AtomSaverKey): AtomSaver<Array<CharSequence>> {
    return object : AtomSaver<Array<CharSequence>> {
        override fun save(bundle: Bundle, value: Array<CharSequence>) {
            bundle.putCharSequenceArray(key, value)
        }

        override fun restore(bundle: Bundle): Array<CharSequence>? {
            return bundle.getCharSequenceArray(key)
        }
    }
}

@PublishedApi
internal fun bundleSaver(key: AtomSaverKey): AtomSaver<Bundle> {
    return object : AtomSaver<Bundle> {
        override fun save(bundle: Bundle, value: Bundle) {
            bundle.putBundle(key, value)
        }

        override fun restore(bundle: Bundle): Bundle? {
            return bundle.getBundle(key)
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
