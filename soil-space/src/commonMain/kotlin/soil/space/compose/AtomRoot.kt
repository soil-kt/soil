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

/**
 * Provides multiple [AtomStore]. You can provide [AtomStore] corresponding to [AtomScope] with different lifecycles.
 *
 * To use the Remember API for handling [Atom] within [content], it is essential to declare [AtomRoot] somewhere in the parent tree.
 * The scoped store is provided to child components using the Remember API via [LocalAtomOwner].
 *
 * @param primary The specified [AtomScope] used preferentially as the default value for [fallbackScope].
 * @param others Additional [AtomScope] and [AtomStore] pairs with lifecycles different from [primary].
 * @param fallbackScope A function that returns an alternate [AtomScope] if a corresponding [AtomScope] for an [Atom] is not found. By default, it returns the [AtomScope] of [primary].
 * @param content The content of the [AtomRoot].
 */
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

/**
 * Provides a single [AtomStore].
 *
 * To use the Remember API for handling [Atom] within [content], it is essential to declare [AtomRoot] somewhere in the parent tree.
 * The [store] is provided to child components using the Remember API via [LocalAtomOwner].
 *
 * @param store An instance of [AtomStore].
 * @param content The content of the [AtomRoot].
 */
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

/**
 * CompositionLocal for [AtomStore].
 */
val LocalAtomOwner = staticCompositionLocalOf<AtomStore> {
    error("CompositionLocal 'LocalAtomOwner' not present")
}
