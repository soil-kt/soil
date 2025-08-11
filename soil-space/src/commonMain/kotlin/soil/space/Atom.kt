// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

package soil.space

import androidx.compose.runtime.Immutable
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
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
        Char::class -> charSaver(saverKey) as AtomSaver<T>
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
 *    | String          | stringListSaver               |
 *    | Int             | intListSaver                  |
 *    | CharSequence    | charSequenceListSaver         |
 *
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@Suppress("UNCHECKED_CAST")
@JvmName("atomWithList")
inline fun <reified T> atom(
    initialValue: List<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<List<T>> {
    val saver = when (T::class) {
        Int::class -> intListSaver(saverKey) as AtomSaver<List<T>>
        CharSequence::class -> charSequenceListSaver(saverKey) as AtomSaver<List<T>>
        String::class -> stringListSaver(saverKey) as AtomSaver<List<T>>
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
        SavedState::class -> bundleArraySaver(saverKey) as AtomSaver<Array<T>>
        else -> null
    }
    return atom(initialValue, saver, scope)
}

@JvmName("atomWithSavedState")
inline fun atom(
    initialValue: SavedState,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<SavedState> {
    return atom(initialValue, bundleSaver(saverKey), scope)
}


/**
 * Interface for saving and restoring values to a [SavedState].
 *
 * Currently, this restoration feature is designed specifically for the Android Platform.
 *
 * @param T The type of the value to be saved and restored.
 */
interface AtomSaver<T> {

    /**
     * Saves a value to a [SavedState].
     *
     * @param state The [SavedState] to save the value to.
     * @param value The value to save.
     */
    fun save(state: SavedState, value: T)

    /**
     * Restores a value from a [SavedState].
     *
     * @param state The [SavedState] to restore the value from.
     * @return The restored value.
     */
    fun restore(state: SavedState): T?
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
inline fun <reified T : Any> serializationSaver(
    key: AtomSaverKey,
    serializer: KSerializer<T> = serializer(),
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT
): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(state: SavedState, value: T) = state.write {
            val savedState = encodeToSavedState(serializer, value, configuration)
            putSavedState(key, savedState)
        }

        override fun restore(state: SavedState): T? = state.read {
            val savedState = getSavedStateOrNull(key) ?: return null
            decodeFromSavedState(serializer, savedState, configuration)
        }
    }
}

@PublishedApi
internal fun stringSaver(key: AtomSaverKey): AtomSaver<String> {
    return object : AtomSaver<String> {
        override fun save(state: SavedState, value: String) = state.write {
            putString(key, value)
        }

        override fun restore(state: SavedState): String? = state.read {
            return getStringOrNull(key)
        }
    }
}

@PublishedApi
internal fun charSequenceSaver(key: AtomSaverKey): AtomSaver<CharSequence> {
    return object : AtomSaver<CharSequence> {
        override fun save(state: SavedState, value: CharSequence) = state.write {
            putCharSequence(key, value)
        }

        override fun restore(state: SavedState): CharSequence? = state.read {
            return getCharSequenceOrNull(key)
        }
    }
}

@PublishedApi
internal fun booleanSaver(key: AtomSaverKey): AtomSaver<Boolean> {
    return object : AtomSaver<Boolean> {
        override fun save(state: SavedState, value: Boolean) = state.write {
            putBoolean(key, value)
        }

        override fun restore(state: SavedState): Boolean? = state.read {
            return getBooleanOrNull(key)
        }
    }
}

@PublishedApi
internal fun intSaver(key: AtomSaverKey): AtomSaver<Int> {
    return object : AtomSaver<Int> {
        override fun save(state: SavedState, value: Int) = state.write {
            putInt(key, value)
        }

        override fun restore(state: SavedState): Int? = state.read {
            return getIntOrNull(key)
        }
    }
}

@PublishedApi
internal fun longSaver(key: AtomSaverKey): AtomSaver<Long> {
    return object : AtomSaver<Long> {
        override fun save(state: SavedState, value: Long) = state.write {
            putLong(key, value)
        }

        override fun restore(state: SavedState): Long? = state.read {
            return getLongOrNull(key)
        }
    }
}

@PublishedApi
internal fun doubleSaver(key: AtomSaverKey): AtomSaver<Double> {
    return object : AtomSaver<Double> {
        override fun save(state: SavedState, value: Double) = state.write {
            putDouble(key, value)
        }

        override fun restore(state: SavedState): Double? = state.read {
            return getDoubleOrNull(key)
        }
    }
}

@PublishedApi
internal fun floatSaver(key: AtomSaverKey): AtomSaver<Float> {
    return object : AtomSaver<Float> {
        override fun save(state: SavedState, value: Float) = state.write {
            putFloat(key, value)
        }

        override fun restore(state: SavedState): Float? = state.read {
            return getFloatOrNull(key)
        }
    }
}

@PublishedApi
internal fun charSaver(key: AtomSaverKey): AtomSaver<Char> {
    return object : AtomSaver<Char> {
        override fun save(state: SavedState, value: Char) = state.write {
            putChar(key, value)
        }

        override fun restore(state: SavedState): Char? = state.read {
            return getCharOrNull(key)
        }
    }
}

@PublishedApi
internal fun intListSaver(key: AtomSaverKey): AtomSaver<List<Int>> {
    return object : AtomSaver<List<Int>> {
        override fun save(state: SavedState, value: List<Int>) = state.write {
            putIntList(key, value)
        }

        override fun restore(state: SavedState): List<Int>? = state.read {
            return getIntListOrNull(key)
        }
    }
}

@PublishedApi
internal fun charSequenceListSaver(key: AtomSaverKey): AtomSaver<List<CharSequence>> {
    return object : AtomSaver<List<CharSequence>> {
        override fun save(state: SavedState, value: List<CharSequence>) = state.write {
            putCharSequenceList(key, value)
        }

        override fun restore(state: SavedState): List<CharSequence>? = state.read {
            return getCharSequenceListOrNull(key)
        }
    }
}

@PublishedApi
internal fun stringListSaver(key: AtomSaverKey): AtomSaver<List<String>> {
    return object : AtomSaver<List<String>> {
        override fun save(state: SavedState, value: List<String>) = state.write {
            putStringList(key, value)
        }

        override fun restore(state: SavedState): List<String>? = state.read {
            return getStringListOrNull(key)
        }
    }
}

@PublishedApi
internal fun booleanArraySaver(key: AtomSaverKey): AtomSaver<BooleanArray> {
    return object : AtomSaver<BooleanArray> {
        override fun save(state: SavedState, value: BooleanArray) = state.write {
            putBooleanArray(key, value)
        }

        override fun restore(state: SavedState): BooleanArray? = state.read {
            return getBooleanArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun charArraySaver(key: AtomSaverKey): AtomSaver<CharArray> {
    return object : AtomSaver<CharArray> {
        override fun save(state: SavedState, value: CharArray) = state.write {
            putCharArray(key, value)
        }

        override fun restore(state: SavedState): CharArray? = state.read {
            return getCharArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun intArraySaver(key: AtomSaverKey): AtomSaver<IntArray> {
    return object : AtomSaver<IntArray> {
        override fun save(state: SavedState, value: IntArray) = state.write {
            putIntArray(key, value)
        }

        override fun restore(state: SavedState): IntArray? = state.read {
            return getIntArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun longArraySaver(key: AtomSaverKey): AtomSaver<LongArray> {
    return object : AtomSaver<LongArray> {
        override fun save(state: SavedState, value: LongArray) = state.write {
            putLongArray(key, value)
        }

        override fun restore(state: SavedState): LongArray? = state.read {
            return getLongArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun floatArraySaver(key: AtomSaverKey): AtomSaver<FloatArray> {
    return object : AtomSaver<FloatArray> {
        override fun save(state: SavedState, value: FloatArray) = state.write {
            putFloatArray(key, value)
        }

        override fun restore(state: SavedState): FloatArray? = state.read {
            return getFloatArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun doubleArraySaver(key: AtomSaverKey): AtomSaver<DoubleArray> {
    return object : AtomSaver<DoubleArray> {
        override fun save(state: SavedState, value: DoubleArray) = state.write {
            putDoubleArray(key, value)
        }

        override fun restore(state: SavedState): DoubleArray? = state.read {
            return getDoubleArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun stringArraySaver(key: AtomSaverKey): AtomSaver<Array<String>> {
    return object : AtomSaver<Array<String>> {
        override fun save(state: SavedState, value: Array<String>) = state.write {
            putStringArray(key, value)
        }

        override fun restore(state: SavedState): Array<String>? = state.read {
            return getStringArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun charSequenceArraySaver(key: AtomSaverKey): AtomSaver<Array<CharSequence>> {
    return object : AtomSaver<Array<CharSequence>> {
        override fun save(state: SavedState, value: Array<CharSequence>) = state.write {
            putCharSequenceArray(key, value)
        }

        override fun restore(state: SavedState): Array<CharSequence>? = state.read {
            return getCharSequenceArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun bundleArraySaver(key: AtomSaverKey): AtomSaver<Array<SavedState>> {
    return object : AtomSaver<Array<SavedState>> {
        override fun save(state: SavedState, value: Array<SavedState>) = state.write {
            putSavedStateArray(key, value)
        }

        override fun restore(state: SavedState): Array<SavedState>? = state.read {
            return getSavedStateArrayOrNull(key)
        }
    }
}

@PublishedApi
internal fun bundleSaver(key: AtomSaverKey): AtomSaver<SavedState> {
    return object : AtomSaver<SavedState> {
        override fun save(state: SavedState, value: SavedState) = state.write {
            putSavedState(key, value)
        }

        override fun restore(state: SavedState): SavedState? = state.read {
            return getSavedStateOrNull(key)
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
