// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import soil.query.core.DataModel
import soil.query.core.uuid

/**
 * Catch for any [DataModel]s to be rejected.
 *
 * @param states The [DataModel]s to catch.
 * @param filter A function to filter the error.
 * @param content The content to display when the query is rejected. By default, it [throws][CatchScope.Throw] the error.
 */
@Composable
inline fun Catch(
    vararg states: DataModel<*>,
    filter: (DataModel<*>) -> Boolean = { true },
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    val err = states
        .filter { it.error != null && filter(it) }
        .minByOrNull { it.errorUpdatedAt }?.error
    if (err != null) {
        with(CatchScope) { content(err) }
    }
}

/**
 * A scope for handling error content within the [Catch] function.
 */
object CatchScope {

    /**
     * Throw propagates the caught exception to a [CatchThrowHost].
     *
     * @param error The caught exception.
     * @param key The key to identify the caught exception.
     * @param host The [CatchThrowHost] to manage the caught exception. By default, it uses the [LocalCatchThrowHost].
     */
    @Composable
    fun Throw(
        error: Throwable,
        key: Any? = null,
        host: CatchThrowHost = LocalCatchThrowHost.current,
    ) {
        val id = remember(Unit) { key ?: uuid() }
        LaunchedEffect(id, error) {
            host[id] = error
        }
        DisposableEffect(id) {
            onDispose {
                host.remove(id)
            }
        }
    }
}
