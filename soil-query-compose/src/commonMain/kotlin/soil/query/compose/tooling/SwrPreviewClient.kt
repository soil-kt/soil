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
import soil.query.SwrClient
import soil.query.core.ErrorRecord

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
    queryPreview: QueryPreviewClient = QueryPreviewClient(emptyMap()),
    mutationPreview: MutationPreviewClient = MutationPreviewClient(emptyMap()),
    override val errorRelay: Flow<ErrorRecord> = flow { }
) : SwrClient, QueryClient by queryPreview, MutationClient by mutationPreview {
    override fun perform(sideEffects: QueryEffect): Job = Job()
    override fun onMount(id: String) = Unit
    override fun onUnmount(id: String) = Unit
}
