// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space.compose

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import soil.space.AtomStore

class AtomViewModel(
    handle: SavedStateHandle
) : ViewModel() {

    val store: AtomStore

    init {
        store = AtomSaveableStore(savedState = handle.get<Bundle>(PROVIDER_KEY))
        handle.setSavedStateProvider(PROVIDER_KEY, store)
    }

    companion object {
        private const val PROVIDER_KEY = "soil.space.atom_vm"
    }
}

// Note:
// ref. https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-apis#compose
@Composable
fun rememberViewModelStore(
    key: String?
): AtomStore {
    val vm = viewModel<AtomViewModel>(
        factory = viewModelFactory {
            initializer {
                AtomViewModel(createSavedStateHandle())
            }
        },
        key = key
    )
    return remember(vm) { vm.store }
}

@Composable
fun rememberViewModelStore(
    viewModelStoreOwner: ViewModelStoreOwner,
    key: String? = null
): AtomStore {
    val vm = viewModel<AtomViewModel>(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = viewModelFactory {
            initializer {
                AtomViewModel(createSavedStateHandle())
            }
        },
        key = key
    )
    return remember(vm) { vm.store }
}
