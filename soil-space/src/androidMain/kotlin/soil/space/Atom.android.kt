// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import android.os.Parcelable
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import java.io.Serializable

/**
 * Creates an [Atom] using [AtomSaverKey] for [Parcelable].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithParcelable")
inline fun <reified T : Parcelable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, parcelableSaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [List] with [Parcelable][T].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithParcelableList")
inline fun <reified T : Parcelable> atom(
    initialValue: List<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<List<T>> {
    return atom(initialValue, parcelableListSaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [Array] with [Parcelable][T].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithParcelableArray")
inline fun <reified T : Parcelable> atom(
    initialValue: Array<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<Array<T>> {
    return atom(initialValue, parcelableArraySaver(saverKey), scope)
}

/**
 * Creates an [Atom] using [AtomSaverKey] for [Serializable].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithJavaSerializable")
inline fun <reified T : Serializable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, javaSerializableSaver(saverKey), scope)
}

@PublishedApi
internal inline fun <reified T : Parcelable> parcelableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(state: SavedState, value: T) = state.write {
            putParcelable(key, value)
        }

        override fun restore(state: SavedState): T? = state.read {
            getParcelableOrNull<T>(key)
        }
    }
}

@PublishedApi
internal inline fun <reified T : Parcelable> parcelableListSaver(key: AtomSaverKey): AtomSaver<List<T>> {
    return object : AtomSaver<List<T>> {
        override fun save(state: SavedState, value: List<T>) = state.write {
            putParcelableList(key, value)
        }

        override fun restore(state: SavedState): List<T>? = state.read {
            getParcelableListOrNull<T>(key)
        }
    }
}

@PublishedApi
internal inline fun <reified T : Parcelable> parcelableArraySaver(key: AtomSaverKey): AtomSaver<Array<T>> {
    return object : AtomSaver<Array<T>> {
        override fun save(state: SavedState, value: Array<T>) = state.write {
            putParcelableArray(key, value)
        }

        override fun restore(state: SavedState): Array<T>? = state.read {
            getParcelableArrayOrNull<T>(key)
        }
    }
}

@PublishedApi
internal inline fun <reified T : Serializable> javaSerializableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(state: SavedState, value: T) = state.write {
            putJavaSerializable(key, value)
        }

        override fun restore(state: SavedState): T? = state.read {
            getJavaSerializableOrNull<T>(key)
        }
    }
}
