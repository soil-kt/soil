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

@Stable
class AtomValue<T>(
    private val state: State<T>,
) : State<T> by state

@Composable
fun <T> rememberAtomValue(
    atom: Atom<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomValue<T> {
    val state = remember(store, atom) { derivedStateOf { store.get(atom) } }
    return remember(state) { AtomValue(state) }
}

@Composable
fun <T> rememberAtomValue(
    atom: AtomRef<T>,
    store: AtomStore = LocalAtomOwner.current
): AtomValue<T> {
    val state = remember(store, atom) { derivedStateOf { store.get(atom) } }
    return remember(state) { AtomValue(state) }
}
