// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.Job

/**
 * An all-in-one [SwrClient] integrating [MutationClient] and [QueryClient] for library users.
 *
 * Swr stands for "stall-while-revalidate".
 */
interface SwrClient : MutationClient, QueryClient {

    /**
     * Executes side effects for queries.
     */
    fun perform(sideEffects: QueryEffect): Job

    /**
     * Executes initialization procedures based on events.
     *
     * Features dependent on the platform are lazily initialized.
     * The following features work correctly by notifying the start of [SwrClient] usage for each mount:
     * - [soil.query.internal.NetworkConnectivity]
     * - [soil.query.internal.MemoryPressure]
     * - [soil.query.internal.WindowVisibility]
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
