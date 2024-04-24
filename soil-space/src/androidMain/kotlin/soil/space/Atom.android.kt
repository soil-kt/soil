// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import androidx.core.os.BundleCompat

actual inline fun <reified T : CommonParcelable> parcelableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: CommonBundle, value: T) {
            bundle.putParcelable(key, value)
        }

        override fun restore(bundle: CommonBundle): T? {
            return if (bundle.containsKey(key)) BundleCompat.getParcelable(bundle, key, T::class.java) else null
        }
    }
}

actual inline fun <reified T : CommonParcelable> parcelableArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<T>> {
    return object : AtomSaver<ArrayList<T>> {
        override fun save(bundle: CommonBundle, value: ArrayList<T>) {
            bundle.putParcelableArrayList(key, value)
        }

        override fun restore(bundle: CommonBundle): ArrayList<T>? {
            return BundleCompat.getParcelableArrayList(bundle, key, T::class.java)
        }
    }
}

@Suppress("UNCHECKED_CAST")
actual inline fun <reified T : CommonParcelable> parcelableArraySaver(key: AtomSaverKey): AtomSaver<Array<T>> {
    return object : AtomSaver<Array<T>> {
        override fun save(bundle: CommonBundle, value: Array<T>) {
            bundle.putParcelableArray(key, value)
        }

        override fun restore(bundle: CommonBundle): Array<T>? {
            return BundleCompat.getParcelableArray(bundle, key, T::class.java) as? Array<T>
        }
    }
}

@Suppress("DEPRECATION")
actual inline fun <reified T : CommonSerializable> serializableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: CommonBundle, value: T) {
            bundle.putSerializable(key, value)
        }

        override fun restore(bundle: CommonBundle): T? {
            // TODO: Compat package starting with Core-ktx Version 1.13
            //  ref. https://issuetracker.google.com/issues/317403466
            return if (bundle.containsKey(key)) bundle.getSerializable(key) as T else null
        }
    }
}
