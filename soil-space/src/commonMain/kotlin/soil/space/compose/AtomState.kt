// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import soil.space.Atom
import soil.space.AtomStore

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

@Composable
fun <T> rememberAtomState(
    atom: Atom<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomState<T> {
    val state = remember(store, atom) { derivedStateOf { store.get(atom) } }
    val update = remember<(T) -> Unit>(store, atom) { { store.set(atom, it) } }
    return remember(state, update) { AtomState(state, update) }
}
