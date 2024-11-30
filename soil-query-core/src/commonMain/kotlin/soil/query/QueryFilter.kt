// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.FilterResolver
import soil.query.core.FilterType
import soil.query.core.InvalidateFilter
import soil.query.core.RemoveFilter
import soil.query.core.ResumeFilter
import soil.query.core.UniqueId

/**
 * Filter for invalidating queries.
 *
 * @see [QueryEffectClient.invalidateQueries], [QueryEffectClient.invalidateQueriesBy]
 */
typealias InvalidateQueriesFilter = InvalidateFilter<QueryModel<*>>

/**
 * Filter for removing queries.
 *
 * @see [QueryEffectClient.removeQueries], [QueryEffectClient.removeQueriesBy]
 */
typealias RemoveQueriesFilter = RemoveFilter<QueryModel<*>>

/**
 * Filter for resuming queries.
 *
 * @see [QueryEffectClient.resumeQueries], [QueryEffectClient.resumeQueriesBy]
 */
typealias ResumeQueriesFilter = ResumeFilter<QueryModel<*>>

/**
 * Filter resolver for queries.
 */
internal class QueryFilterResolver(
    private val store: Map<UniqueId, Query<*>>,
    private val cache: QueryCache
) : FilterResolver<QueryModel<*>> {

    override fun resolveKeys(type: FilterType): Set<UniqueId> = when (type) {
        FilterType.Active -> store.keys
        FilterType.Inactive -> cache.keys
    }

    override fun resolveValue(type: FilterType, id: UniqueId): QueryModel<*>? = when (type) {
        FilterType.Active -> store[id]?.state?.value
        FilterType.Inactive -> cache[id]
    }
}
