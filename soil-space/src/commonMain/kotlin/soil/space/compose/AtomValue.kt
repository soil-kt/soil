// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import soil.space.Atom
import soil.space.AtomRef
import soil.space.AtomStore

/**
 * [State] for handling the state values of [Atom] managed by [AtomStore].
 *
 * @param T The type of the state value.
 * @param state The [State] used to retrieve the state value.
 */
@Stable
class AtomValue<T>(
    private val state: State<T>,
) : State<T> by state


/**
 * Remember an [AtomValue] for the specified [Atom] reference key.
 *
 * @param T The type of the state value.
 * @param atom The reference key.
 * @param store The [AtomStore] that manages the state using the [atom] reference key. By default, it resolves using [LocalAtomOwner].
 * @return The [AtomValue] for the specified [Atom] reference key.
 */
@Composable
fun <T> rememberAtomValue(
    atom: Atom<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomValue<T> {
    val state = remember(store, atom) { derivedStateOf { store.get(atom) } }
    return remember(state) { AtomValue(state) }
}

/**
 * Remember an [AtomValue] for the specified [AtomRef] reference key.
 *
 * @param T The type of the state value.
 * @param atom The reference key.
 * @param store The [AtomStore] that manages the state using the [atom] reference key. By default, it resolves using [LocalAtomOwner].
 * @return The [AtomValue] for the specified [AtomRef] reference key.
 */
@Composable
fun <T> rememberAtomValue(
    atom: AtomRef<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomValue<T> {
    val state = remember(store, atom) { derivedStateOf { store.get(atom) } }
    return remember(state) { AtomValue(state) }
}
