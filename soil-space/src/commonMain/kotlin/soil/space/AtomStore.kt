// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

interface AtomStore : AtomSelector {
    fun <T> set(atom: Atom<T>, value: T)

    override fun <T> get(atom: AtomRef<T>): T {
        val fn = atom.block
        return with(this) { fn() }
    }
}
