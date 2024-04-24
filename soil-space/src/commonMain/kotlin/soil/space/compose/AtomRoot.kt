// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import soil.space.Atom
import soil.space.AtomScope
import soil.space.AtomStore

@Composable
fun AtomRoot(
    primary: Pair<AtomScope, AtomStore>,
    vararg others: Pair<AtomScope, AtomStore>,
    fallbackScope: (Atom<*>) -> AtomScope = { primary.first },
    content: @Composable () -> Unit
) {
    val store = remember(primary, others, fallbackScope) {
        ScopedAtomStore(stores = mapOf(primary, *others), fallbackScope = fallbackScope)
    }
    CompositionLocalProvider(LocalAtomOwner provides store) {
        content()
    }
}

@Composable
fun AtomRoot(
    store: AtomStore,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAtomOwner provides store) {
        content()
    }
}

private class ScopedAtomStore(
    private val stores: Map<AtomScope, AtomStore>,
    private val fallbackScope: (Atom<*>) -> AtomScope
) : AtomStore {

    override fun <T> get(atom: Atom<T>): T {
        return store(atom).get(atom)
    }

    override fun <T> set(atom: Atom<T>, value: T) {
        store(atom).set(atom, value)
    }

    private fun store(atom: Atom<*>): AtomStore {
        return stores[atom.scope] ?: checkNotNull(stores[fallbackScope(atom)])
    }
}

val LocalAtomOwner = staticCompositionLocalOf<AtomStore> {
    error("CompositionLocal 'LocalAtomOwner' not present")
}
