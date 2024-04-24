// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.SurrogateKey

interface QueryFilter {

    val type: QueryFilterType?

    val keys: Array<SurrogateKey>?

    val predicate: ((QueryModel<*>) -> Boolean)?
}

class InvalidateQueriesFilter(
    override val type: QueryFilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((QueryModel<*>) -> Boolean)? = null,
) : QueryFilter

class RemoveQueriesFilter(
    override val type: QueryFilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((QueryModel<*>) -> Boolean)? = null,
) : QueryFilter

class ResumeQueriesFilter(
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: (QueryModel<*>) -> Boolean
) : QueryFilter {
    override val type: QueryFilterType = QueryFilterType.Active
}

enum class QueryFilterType {
    Active,
    Inactive
}
