// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import androidx.compose.runtime.Immutable

/**
 * Interface for retrieving the value associated with the [Atom]s reference key.
 *
 * This interface is used as part of [AtomRef].
 */
interface AtomSelector {

    /**
     * Retrieves the value associated with the specified [Atom] reference key.
     *
     * @param T The type of the value to be retrieved.
     * @param atom The [Atom] reference key for the value to be retrieved.
     * @return The retrieved value.
     */
    fun <T> get(atom: Atom<T>): T

    /**
     * Retrieves the value associated with the specified [AtomRef] reference key.
     *
     * @param T The type of the value to be retrieved.
     * @param atom The [AtomRef] reference key for the value to be retrieved.
     * @return The retrieved value.
     */
    fun <T> get(atom: AtomRef<T>): T
}

/**
 * AtomRef holds a value derived from one or more [Atom] references as a [block] function.
 *
 * @param T The type of the value to be retrieved.
 * @property block The function that retrieves the value derived from one or more [Atom] references key.
 */
@Immutable
class AtomRef<T> internal constructor(
    val block: AtomSelector.() -> T
)

/**
 * Creates an [AtomRef] with the specified [block] function.
 *
 * Usage:
 *
 * ```kotlin
 * val counter1Atom = atom(0)
 * val counter2Atom = atom(0)
 * val sumAtom = atom {
 *     get(counter1Atom) + get(counter2Atom)
 * }
 * ```
 *
 * @param T The type of the value to be retrieved.
 * @param block The function that retrieves the value derived from one or more [Atom] references key.
 * @return The created [AtomRef].
 */
fun <T> atom(block: AtomSelector.() -> T): AtomRef<T> {
    return AtomRef(block)
}
