// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.SurrogateKey

/**
 * Interface for filtering side effect queries by [QueryMutableClient].
 */
interface QueryFilter {

    /**
     * Management category of queries to be filtered.
     *
     * If unspecified, all [QueryFilterType]s are targeted.
     */
    val type: QueryFilterType?

    /**
     * Tag keys of queries to be filtered.
     */
    val keys: Array<SurrogateKey>?

    /**
     * Further conditions to narrow down the filtering targets based on fields of [QueryModel].
     */
    val predicate: ((QueryModel<*>) -> Boolean)?
}

/**
 * Filter for invalidating queries.
 *
 * @see [QueryMutableClient.invalidateQueries], [QueryMutableClient.invalidateQueriesBy]
 */
class InvalidateQueriesFilter(
    override val type: QueryFilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((QueryModel<*>) -> Boolean)? = null,
) : QueryFilter

/**
 * Filter for removing queries.
 *
 * @see [QueryMutableClient.removeQueries], [QueryMutableClient.removeQueriesBy]
 */
class RemoveQueriesFilter(
    override val type: QueryFilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((QueryModel<*>) -> Boolean)? = null,
) : QueryFilter

/**
 * Filter for resuming queries.
 *
 * @see [QueryMutableClient.resumeQueries], [QueryMutableClient.resumeQueriesBy]
 */
class ResumeQueriesFilter(
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: (QueryModel<*>) -> Boolean
) : QueryFilter {
    override val type: QueryFilterType = QueryFilterType.Active
}

/**
 * Query management categories for filtering.
 */
enum class QueryFilterType {
    Active,
    Inactive
}
