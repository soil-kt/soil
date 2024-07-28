// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.internal.Actor

/**
 * Mutation as the base interface for an [MutationClient] implementations.
 *
 * @param T Type of the return value from the mutation.
 */
interface Mutation<T> : Actor {

    /**
     * [Shared Flow][SharedFlow] to receive mutation [events][MutationEvent].
     */
    val event: SharedFlow<MutationEvent>

    /**
     * [State Flow][StateFlow] to receive the current state of the mutation.
     */
    val state: StateFlow<MutationState<T>>

    /**
     * [Send Channel][SendChannel] to manipulate the state of the mutation.
     */
    val command: SendChannel<MutationCommand<T>>
}

/**
 * Events occurring in the mutation.
 */
enum class MutationEvent {
    Ping
}
