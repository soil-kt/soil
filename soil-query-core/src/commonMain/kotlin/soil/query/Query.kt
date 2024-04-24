// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Query<T> {
    val actor: Flow<*>
    val event: SharedFlow<QueryEvent>
    val state: StateFlow<QueryState<T>>
    val command: SendChannel<QueryCommand<T>>
}

enum class QueryEvent {
    Invalidate,
    Resume,
    Ping
}
