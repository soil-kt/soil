// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor

/**
 * A reference to an Query for [QueryKey].
 *
 * @param T Type of data to retrieve.
 */
interface QueryRef<T> : Actor {

    val key: QueryKey<T>
    val options: QueryOptions
    val state: StateFlow<QueryState<T>>

    /**
     * Sends a [QueryCommand] to the Actor.
     */
    suspend fun send(command: QueryCommand<T>)
}

/**
 * Invalidates the Query.
 *
 * Calling this function will invalidate the retrieved data of the Query,
 * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
 */
suspend fun <T> QueryRef<T>.invalidate() {
    send(QueryCommands.Invalidate(key, state.value.revision))
}

/**
 * Resumes the Query.
 */
suspend fun <T> QueryRef<T>.resume() {
    send(QueryCommands.Connect(key, state.value.revision))
}
