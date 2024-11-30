// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import soil.query.MutationClient
import soil.query.QueryClient
import soil.query.QueryEffect
import soil.query.SubscriptionClient
import soil.query.SwrClient
import soil.query.SwrClientPlus
import soil.query.core.Effect
import soil.query.core.ErrorRecord
import soil.query.core.MemoryPressureLevel

/**
 * Provides the ability to preview specific queries and mutations for composable previews.
 *
 * ```
 * val client = SwrPreviewClient(..)
 * SwrClientProvider(client = client) {
 *   // Composable previews
 * }
 * ```
 */
@Stable
class SwrPreviewClient(
    query: QueryPreviewClient = QueryPreviewClient(emptyMap()),
    mutation: MutationPreviewClient = MutationPreviewClient(emptyMap()),
    subscription: SubscriptionPreviewClient = SubscriptionPreviewClient(emptyMap()),
    override val errorRelay: Flow<ErrorRecord> = flow { }
) : SwrClient, SwrClientPlus, QueryClient by query, MutationClient by mutation, SubscriptionClient by subscription {
    override fun gc(level: MemoryPressureLevel) = Unit
    override fun purgeAll() = Unit

    @Deprecated("Use effect(block: Effect) instead.", replaceWith = ReplaceWith("effect(block)"))
    override fun perform(sideEffects: QueryEffect): Job = Job()
    override fun effect(block: Effect): Job = Job()
    override fun onMount(id: String) = Unit
    override fun onUnmount(id: String) = Unit
}
