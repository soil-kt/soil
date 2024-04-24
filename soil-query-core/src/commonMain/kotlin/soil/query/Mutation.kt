// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Mutation<T> {
    val actor: Flow<*>
    val event: SharedFlow<MutationEvent>
    val state: StateFlow<MutationState<T>>
    val command: SendChannel<MutationCommand<T>>
}

enum class MutationEvent {
    Ping
}
