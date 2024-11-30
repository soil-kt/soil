// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Interface for filtering side effect
 */
interface Filter<T : DataModel<*>> {

    /**
     * Management category of queries to be filtered.
     *
     * If unspecified, all [FilterType]s are targeted.
     */
    val type: FilterType?

    /**
     * Tag keys of queries to be filtered.
     */
    val keys: Array<SurrogateKey>?

    /**
     * Further conditions to narrow down the filtering targets based on fields of [T].
     */
    val predicate: ((T) -> Boolean)?
}

/**
 * Filter for invalidating targets.
 */
class InvalidateFilter<T : DataModel<*>>(
    override val type: FilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((T) -> Boolean)? = null,
) : Filter<T>

/**
 * Filter for removing targets.
 */
class RemoveFilter<T : DataModel<*>>(
    override val type: FilterType? = null,
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: ((T) -> Boolean)? = null,
) : Filter<T>

/**
 * Filter for resuming targets.
 */
class ResumeFilter<T : DataModel<*>>(
    override val keys: Array<SurrogateKey>? = null,
    override val predicate: (T) -> Boolean
) : Filter<T> {
    override val type: FilterType = FilterType.Active
}

enum class FilterType {
    Active,
    Inactive
}
