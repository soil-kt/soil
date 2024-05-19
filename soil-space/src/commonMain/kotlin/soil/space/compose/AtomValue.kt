// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import soil.space.Atom
import soil.space.AtomNode
import soil.space.AtomRef
import soil.space.AtomSelector
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
    val node = remember(store, atom) { store.bind(atom) }
    val state = node.state.collectAsState()
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
    val computed = remember(store, atom) { Computed(store) }
    val state = remember(computed) { mutableStateOf(computed.ensureInit(atom)) }
    LaunchedEffect(computed) {
        combine(computed.nodes.map { it.state }) { computed.get(atom) }
            .distinctUntilChanged()
            .collect { value ->
                if (state.value != value) {
                    state.value = value
                }
            }
    }
    return remember(state) { AtomValue(state) }
}

private class Computed(
    private val store: AtomStore
) : AtomSelector {

    private val _nodes: MutableSet<AtomNode<*>> = mutableSetOf()
    val nodes: Set<AtomNode<*>> get() = _nodes

    private var isCapturing: Boolean = false

    fun <T> ensureInit(atom: AtomRef<T>): T {
        isCapturing = true
        val value = get(atom)
        isCapturing = false
        return value
    }

    override fun <T> get(atom: Atom<T>): T {
        val node = store.bind(atom)
        if (isCapturing) {
            _nodes.add(node)
        }
        return node.state.value
    }

    override fun <T> get(atom: AtomRef<T>): T {
        return atom.block.invoke(this)
    }
}
