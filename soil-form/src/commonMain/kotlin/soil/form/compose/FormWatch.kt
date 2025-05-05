package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.flow.dropWhile
import soil.form.FormData

@Composable
fun <T : Any, S> Form<T>.watch(block: FormData<T>.() -> S): S {
    var value by remember { mutableStateOf(Snapshot.withoutReadObservation { state.block() }) }
    LaunchedEffect(Unit) {
        snapshotFlow { state.block() }
            .dropWhile { it == value }
            .collect { newValue ->
                value = newValue
            }
    }
    return value
}
