// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import android.os.Parcelable
import androidx.core.bundle.Bundle
import androidx.core.os.BundleCompat
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
 * Creates an [Atom] using [AtomSaverKey] for [ArrayList] with [Parcelable][T].
 *
 * @param T The type of the value to be stored.
 * @param initialValue The initial value to be stored.
 * @param saverKey The key to be used to save and restore the value.
 * @param scope The scope to be used to manage the value.
 * @return The created Atom.
 */
@JvmName("atomWithParcelableArrayList")
inline fun <reified T : Parcelable> atom(
    initialValue: ArrayList<T>,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<ArrayList<T>> {
    return atom(initialValue, parcelableArrayListSaver(saverKey), scope)
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
@JvmName("atomWithSerializable")
inline fun <reified T : Serializable> atom(
    initialValue: T,
    saverKey: AtomSaverKey,
    scope: AtomScope? = null
): Atom<T> {
    return atom(initialValue, serializableSaver(saverKey), scope)
}

@PublishedApi
internal inline fun <reified T : Parcelable> parcelableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: Bundle, value: T) {
            bundle.putParcelable(key, value)
        }

        override fun restore(bundle: Bundle): T? {
            return if (bundle.containsKey(key)) BundleCompat.getParcelable(bundle, key, T::class.java) else null
        }
    }
}

@PublishedApi
internal inline fun <reified T : Parcelable> parcelableArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<T>> {
    return object : AtomSaver<ArrayList<T>> {
        override fun save(bundle: Bundle, value: ArrayList<T>) {
            bundle.putParcelableArrayList(key, value)
        }

        override fun restore(bundle: Bundle): ArrayList<T>? {
            return BundleCompat.getParcelableArrayList(bundle, key, T::class.java)
        }
    }
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified T : Parcelable> parcelableArraySaver(key: AtomSaverKey): AtomSaver<Array<T>> {
    return object : AtomSaver<Array<T>> {
        override fun save(bundle: Bundle, value: Array<T>) {
            bundle.putParcelableArray(key, value)
        }

        override fun restore(bundle: Bundle): Array<T>? {
            return BundleCompat.getParcelableArray(bundle, key, T::class.java) as? Array<T>
        }
    }
}

@Suppress("DEPRECATION")
@PublishedApi
internal inline fun <reified T : Serializable> serializableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: Bundle, value: T) {
            bundle.putSerializable(key, value)
        }

        override fun restore(bundle: Bundle): T? {
            // TODO: Compat package starting with Core-ktx Version 1.13
            //  ref. https://issuetracker.google.com/issues/317403466
            return if (bundle.containsKey(key)) bundle.getSerializable(key) as T else null
        }
    }
}
