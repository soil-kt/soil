package soil.space

import kotlinx.coroutines.flow.StateFlow

interface AtomNode<T> {
    val state: StateFlow<T>
    val update: (value: T) -> Unit
}
