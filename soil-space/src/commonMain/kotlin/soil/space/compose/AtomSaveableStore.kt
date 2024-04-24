// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import soil.space.Atom
import soil.space.AtomStore
import soil.space.CommonBundle
import soil.space.CommonSavedStateProvider

@Suppress("SpellCheckingInspection")
class AtomSaveableStore(
    private val savedState: CommonBundle? = null
) : AtomStore, CommonSavedStateProvider {

    private val stateMap: MutableMap<Atom<*>, ManagedState<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(atom: Atom<T>): T {
        var state = stateMap[atom] as? ManagedState<T>
        if (state == null) {
            val newState = ManagedState(atom).also { stateMap[atom] = it }
            if (savedState != null) {
                newState.onRestore(savedState)
            }
            state = newState
        }
        return state.value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> set(atom: Atom<T>, value: T) {
        val state = stateMap[atom] as? ManagedState<T>
        state?.value = value
    }

    override fun saveState(): CommonBundle {
        val box = savedState ?: CommonBundle()
        stateMap.keys.forEach { atom ->
            val value = stateMap[atom] as ManagedState<*>
            value.onSave(box)
        }
        return box
    }

    class ManagedState<T>(
        private val atom: Atom<T>,
    ) : MutableState<T> by mutableStateOf(atom.initialValue) {

        fun onSave(bundle: CommonBundle) {
            atom.saver?.save(bundle, value)
        }

        fun onRestore(bundle: CommonBundle) {
            atom.saver?.restore(bundle)?.let { value = it }
        }
    }
}

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
