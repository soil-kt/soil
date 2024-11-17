package soil.space.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import soil.space.AtomScope
import soil.space.AtomStore
import soil.space.AtomStoreOwner

inline fun <T> defineStore(
    key: String? = null,
    scope: AtomScope? = null,
    crossinline block: @Composable AtomStore.() -> T
): @Composable (AtomStoreOwner) -> T = { owner ->
    with(owner.createStore(key, scope)) { block() }
}


val useFooStore = defineStore<CounterStore> {


    CounterStore(
        value = 0,
        increment = { }
    )
}

@Immutable
data class CounterStore(
    val value: Int,
    val increment: () -> Unit
)

// AtomStoreDefinition
