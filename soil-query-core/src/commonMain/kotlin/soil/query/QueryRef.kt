// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.core.Actor
import soil.query.core.Marker
import soil.query.core.awaitOrNull

/**
 * A reference to an Query for [QueryKey].
 *
 * @param T Type of data to retrieve.
 */
interface QueryRef<T> : Actor {

    /**
     * A unique identifier used for managing [QueryKey].
     */
    val id: QueryId<T>

    /**
     * [State Flow][StateFlow] to receive the current state of the query.
     */
    val state: StateFlow<QueryState<T>>

    /**
     * Resumes the Query.
     */
    suspend fun resume()

    /**
     * Invalidates the Query.
     *
     * Calling this function will invalidate the retrieved data of the Query,
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate()
}

/**
 * Creates a new [QueryRef] instance.
 *
 * @param key The [QueryKey] for the Query.
 * @param marker The Marker specified in [QueryClient.getQuery].
 * @param query The Query to create a reference.
 */
fun <T> QueryRef(
    key: QueryKey<T>,
    marker: Marker,
    query: Query<T>
): QueryRef<T> {
    return QueryRefImpl(key, marker, query)
}

private class QueryRefImpl<T>(
    private val key: QueryKey<T>,
    private val marker: Marker,
    private val query: Query<T>
) : QueryRef<T> {

    override val id: QueryId<T>
        get() = key.id

    override val state: StateFlow<QueryState<T>>
        get() = query.state

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            query.launchIn(this)
            query.event.collect(::handleEvent)
        }
    }

    override suspend fun resume() {
        val deferred = CompletableDeferred<T>()
        send(QueryCommands.Connect(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    override suspend fun invalidate() {
        val deferred = CompletableDeferred<T>()
        send(QueryCommands.Invalidate(key, state.value.revision, marker, deferred::completeWith))
        deferred.awaitOrNull()
    }

    private suspend fun send(command: QueryCommand<T>) {
        query.command.send(command)
    }


    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
        }
    }
}
