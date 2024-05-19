// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import soil.space.Atom
import soil.space.AtomNode
import soil.space.AtomStore
import soil.space.CommonBundle
import soil.space.CommonSavedStateProvider

/**
 * A [AtomStore] implementation that saves and restores the state of [Atom]s.
 *
 * @param savedState The saved state to be restored.
 */
@Suppress("SpellCheckingInspection")
class AtomSaveableStore(
    private val savedState: CommonBundle? = null
) : AtomStore, CommonSavedStateProvider {

    private val nodeMap: MutableMap<Atom<*>, ManagedAtomNode<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> bind(atom: Atom<T>): AtomNode<T> {
        var node = nodeMap[atom] as? ManagedAtomNode<T>
        if (node == null) {
            val newState = ManagedAtomNode(atom).also { nodeMap[atom] = it }
            if (savedState != null) {
                newState.onRestore(savedState)
            }
            node = newState
        }
        return node
    }

    override fun saveState(): CommonBundle {
        val box = savedState ?: CommonBundle()
        nodeMap.keys.forEach { atom ->
            val value = nodeMap[atom] as ManagedAtomNode<*>
            value.onSave(box)
        }
        return box
    }

    class ManagedAtomNode<T>(
        private val atom: Atom<T>,
        override val state: MutableStateFlow<T> = MutableStateFlow(atom.initialValue),
        override val update: (value: T) -> Unit = { state.value = it }
    ) : AtomNode<T> {

        fun onSave(bundle: CommonBundle) {
            atom.saver?.save(bundle, state.value)
        }

        fun onRestore(bundle: CommonBundle) {
            atom.saver?.restore(bundle)?.let { state.value = it }
        }
    }
}

/**
 * Remember a [AtomSaveableStore] that saves and restores the state of [Atom]s.
 *
 * **Note:**
 * [LocalSaveableStateRegistry] is required to save and restore the state.
 * If [LocalSaveableStateRegistry] is not found, the state will not be saved and restored.
 *
 * @param key The key to save and restore the state. By default, it resolves using [currentCompositeKeyHash].
 * @return The remembered [AtomSaveableStore].
 */
@Suppress("SpellCheckingInspection")
@Composable
fun rememberSaveableStore(key: String? = null): AtomStore {
    val finalKey = if (!key.isNullOrEmpty()) {
        key
    } else {
        currentCompositeKeyHash.toString(MaxSupportedRadix)
    }
    val registry = LocalSaveableStateRegistry.current
    val store = remember(registry) {
        AtomSaveableStore(registry?.consumeRestored(finalKey) as? CommonBundle)
    }
    if (registry != null) {
        DisposableEffect(registry, finalKey, store) {
            val valueProvider = { store.saveState() }
            val entry = if (registry.canBeSaved(valueProvider())) {
                registry.registerProvider(finalKey, valueProvider)
            } else {
                null
            }
            onDispose {
                entry?.unregister()
            }
        }
    }
    return store
}

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
