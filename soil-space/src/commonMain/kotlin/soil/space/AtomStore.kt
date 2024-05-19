// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

/**
 * An interface for storing and retrieving atoms.
 */
interface AtomStore : AtomSelector {

    /**
     * Set the value of an atom.
     *
     * @param T The type of the value to be stored.
     * @param atom The reference key.
     * @param value The value to be stored.
     */
    fun <T> set(atom: Atom<T>, value: T)

    override fun <T> get(atom: AtomRef<T>): T {
        val fn = atom.block
        return with(this) { fn() }
    }
}
