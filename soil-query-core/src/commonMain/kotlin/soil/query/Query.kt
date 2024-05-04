// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Query as the base interface for an [QueryClient] implementations.
 *
 * @param T Type of the return value from the query.
 */
interface Query<T> {

    /**
     * Coroutine [Flow] to launch the actor.
     */
    val actor: Flow<*>

    /**
     * [Shared Flow][SharedFlow] to receive query [events][QueryEvent].
     */
    val event: SharedFlow<QueryEvent>

    /**
     * [State Flow][StateFlow] to receive the current state of the query.
     */
    val state: StateFlow<QueryState<T>>

    /**
     * [Send Channel][SendChannel] to manipulate the state of the query.
     */
    val command: SendChannel<QueryCommand<T>>
}

/**
 * Events occurring in the query.
 */
enum class QueryEvent {
    Invalidate,
    Resume,
    Ping
}
