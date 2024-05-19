// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

/**
 * An interface for storing and retrieving atoms.
 */
interface AtomStore {

    fun <T> bind(atom: Atom<T>): AtomNode<T>
}
