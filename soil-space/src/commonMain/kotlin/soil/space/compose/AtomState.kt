// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import soil.space.Atom
import soil.space.AtomStore

/**
 * [MutableState] for handling the state values of [Atom] managed by [AtomStore].
 *
 * @param T The type of the state value.
 * @param state The [State] used to retrieve the state value.
 * @param update A function to update the state value.
 */
@Stable
class AtomState<T>(
    private val state: State<T>,
    private val update: (T) -> Unit,
) : MutableState<T> {
    override var value: T
        get() = state.value
        set(value) = update(value)

    override operator fun component1(): T = value
    override operator fun component2(): (T) -> Unit = update
}

/**
 * Remember an [AtomState] for the specified [Atom] reference key.
 *
 * @param T The type of the state value.
 * @param atom The reference key.
 * @param store The [AtomStore] that manages the state using the [atom] reference key. By default, it resolves using [LocalAtomOwner].
 * @return The [AtomState] for the specified [Atom] reference key.
 */
@Composable
fun <T> rememberAtomState(
    atom: Atom<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomState<T> {
    val node = remember(store, atom) { store.bind(atom) }
    val state = node.state.collectAsState()
    return remember(state, node.update) { AtomState(state, node.update) }
}
