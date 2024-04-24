// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

actual inline fun <reified T : CommonParcelable> parcelableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: CommonBundle, value: T) {
            bundle.putParcelable(key, value)
        }

        override fun restore(bundle: CommonBundle): T? {
            return if (bundle.containsKey(key)) bundle.getParcelable(key) else null
        }
    }
}

actual inline fun <reified T : CommonParcelable> parcelableArrayListSaver(key: AtomSaverKey): AtomSaver<ArrayList<T>> {
    return object : AtomSaver<ArrayList<T>> {
        override fun save(bundle: CommonBundle, value: ArrayList<T>) {
            bundle.putParcelableArrayList(key, value)
        }

        override fun restore(bundle: CommonBundle): ArrayList<T>? {
            return bundle.getParcelableArrayList(key)
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
            return bundle.getParcelableArray(key) as? Array<T>
        }
    }
}

actual inline fun <reified T : CommonSerializable> serializableSaver(key: AtomSaverKey): AtomSaver<T> {
    return object : AtomSaver<T> {
        override fun save(bundle: CommonBundle, value: T) {
            bundle.putSerializable(key, value)
        }

        override fun restore(bundle: CommonBundle): T? {
            return if (bundle.containsKey(key)) bundle.getSerializable(key) as T else null
        }
    }
}
