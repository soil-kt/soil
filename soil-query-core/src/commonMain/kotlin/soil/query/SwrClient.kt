// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import soil.query.core.ErrorRecord
import soil.query.core.MemoryPressureLevel

/**
 * An all-in-one [SwrClient] integrating [MutationClient] and [QueryClient] for library users.
 *
 * Swr stands for "stall-while-revalidate".
 */
interface SwrClient : MutationClient, QueryClient {

    /**
     * Provides a unified feedback mechanism for all Query/Mutation errors that occur within the client.
     *
     * For example, by collecting errors on the foreground screen,
     * you can display an error message on the screen using a Toast or similar when an error occurs.
     */
    val errorRelay: Flow<ErrorRecord>

    /**
     * Releases data in memory based on the specified [level].
     */
    fun gc(level: MemoryPressureLevel = MemoryPressureLevel.Low)

    /**
     * Removes all queries and mutations from the in-memory stored data.
     *
     * **Note:**
     * If there are any active queries or mutations, they will be stopped as well.
     * This method should only be used for full resets, such as during sign-out.
     */
    fun purgeAll()

    /**
     * Executes side effects for queries.
     */
    fun perform(sideEffects: QueryEffect): Job

    /**
     * Executes initialization procedures based on events.
     *
     * Features dependent on the platform are lazily initialized.
     * The following features work correctly by notifying the start of [SwrClient] usage for each mount:
     * - [soil.query.core.NetworkConnectivity]
     * - [soil.query.core.MemoryPressure]
     * - [soil.query.core.WindowVisibility]
     *
     * @param id Unique string for each mount point.
     */
    fun onMount(id: String)

    /**
     * Executes cleanup procedures based on events.
     *
     * @param id Unique string for each mount point.
     */
    fun onUnmount(id: String)
}
