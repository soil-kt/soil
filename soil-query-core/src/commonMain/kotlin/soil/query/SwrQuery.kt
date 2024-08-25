// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.core.Marker

internal class SwrQuery<T>(
    override val key: QueryKey<T>,
    override val marker: Marker,
    private val query: Query<T>
) : QueryRef<T> {

    override val state: StateFlow<QueryState<T>>
        get() = query.state

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            query.launchIn(this)
            query.event.collect(::handleEvent)
        }
    }

    override suspend fun send(command: QueryCommand<T>) {
        query.command.send(command)
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
