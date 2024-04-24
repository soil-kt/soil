// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import soil.query.QueryModel
import soil.query.internal.uuid

@Composable
fun Catch(
    state: Loadable<*>,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    Catch(
        state = state,
        filterIsInstance = { it },
        isEnabled = isEnabled,
        content = content
    )
}

@Composable
fun <T : Throwable> Catch(
    state: Loadable<*>,
    filterIsInstance: (err: Throwable) -> T?,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: T) -> Unit = { Throw(error = it) }
) {
    when (state) {
        is Loadable.Rejected -> {
            val err = remember(state.error, isEnabled) {
                state.error.takeIf { isEnabled }?.let(filterIsInstance)
            }
            if (err != null) {
                with(CatchScope) { content(err) }
            }
        }

        is Loadable.Fulfilled,
        is Loadable.Pending -> Unit
    }
}

@Composable
fun Catch(
    state: QueryModel<*>,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    Catch(
        state = state,
        filterIsInstance = { it },
        isEnabled = isEnabled,
        content = content
    )
}

@Composable
fun Catch(
    state1: QueryModel<*>,
    state2: QueryModel<*>,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    Catch(
        state1 = state1,
        state2 = state2,
        filterIsInstance = { it },
        isEnabled = isEnabled,
        content = content
    )
}

@Composable
fun Catch(
    state1: QueryModel<*>,
    state2: QueryModel<*>,
    state3: QueryModel<*>,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    Catch(
        state1 = state1,
        state2 = state2,
        state3 = state3,
        filterIsInstance = { it },
        isEnabled = isEnabled,
        content = content
    )
}

@Composable
fun Catch(
    vararg states: QueryModel<*>,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: Throwable) -> Unit = { Throw(error = it) }
) {
    Catch(
        states = states,
        filterIsInstance = { it },
        isEnabled = isEnabled,
        content = content
    )
}

@Composable
fun <T : Throwable> Catch(
    state: QueryModel<*>,
    filterIsInstance: (err: Throwable) -> T?,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: T) -> Unit = { Throw(error = it) }
) {
    val err = state.error.takeIf { isEnabled }?.let(filterIsInstance)
    if (err != null) {
        with(CatchScope) { content(err) }
    }
}

@Composable
fun <T : Throwable> Catch(
    state1: QueryModel<*>,
    state2: QueryModel<*>,
    filterIsInstance: (err: Throwable) -> T?,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: T) -> Unit = { Throw(error = it) }
) {
    val err = listOf(state1, state2).takeIf { isEnabled }
        ?.firstNotNullOfOrNull { it.error }
        ?.let(filterIsInstance)

    if (err != null) {
        with(CatchScope) { content(err) }
    }
}

@Composable
fun <T : Throwable> Catch(
    state1: QueryModel<*>,
    state2: QueryModel<*>,
    state3: QueryModel<*>,
    filterIsInstance: (err: Throwable) -> T?,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: T) -> Unit = { Throw(error = it) }
) {
    val err = listOf(state1, state2, state3).takeIf { isEnabled }
        ?.firstNotNullOfOrNull { it.error }
        ?.let(filterIsInstance)

    if (err != null) {
        with(CatchScope) { content(err) }
    }
}

@Composable
fun <T : Throwable> Catch(
    vararg states: QueryModel<*>,
    filterIsInstance: (err: Throwable) -> T?,
    isEnabled: Boolean = true,
    content: @Composable CatchScope.(err: T) -> Unit = { Throw(error = it) }
) {
    val err = states.takeIf { isEnabled }
        ?.firstNotNullOfOrNull { it.error }
        ?.let(filterIsInstance)

    if (err != null) {
        with(CatchScope) { content(err) }
    }
}


object CatchScope {
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
