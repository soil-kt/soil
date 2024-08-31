// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor

/**
 * Mutation as the base interface for an [MutationClient] implementations.
 *
 * @param T Type of the return value from the mutation.
 */
internal interface Mutation<T> : Actor {

    /**
     * The MutationOptions configured for the mutation.
     */
    val options: MutationOptions

    /**
     * [State Flow][StateFlow] to receive the current state of the mutation.
     */
    val state: StateFlow<MutationState<T>>

    /**
     * [Send Channel][SendChannel] to manipulate the state of the mutation.
     */
    val command: SendChannel<MutationCommand<T>>
}
