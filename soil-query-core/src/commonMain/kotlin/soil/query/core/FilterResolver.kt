// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * An interface for resolving the data sources targeted by a [Filter].
 */
internal interface FilterResolver<T : DataModel<*>> {

    /**
     * Resolves a list of [UniqueId]s managed under the specified [FilterType].
     */
    fun resolveKeys(type: FilterType): Set<UniqueId>

    /**
     * Resolves the data corresponding to the [UniqueId] managed under the specified [FilterType].
     */
    fun resolveValue(type: FilterType, id: UniqueId): T?
}


internal fun <T : DataModel<*>> FilterResolver<T>.forEach(
    filter: Filter<T>,
    action: (UniqueId, FilterType) -> Unit
) {
    if (filter.type == null || filter.type == FilterType.Active) {
        forEach(FilterType.Active, filter.keys, filter.predicate) { id ->
            action(id, FilterType.Active)
        }
    }
    if (filter.type == null || filter.type == FilterType.Inactive) {
        forEach(FilterType.Inactive, filter.keys, filter.predicate) { id ->
            action(id, FilterType.Inactive)
        }
    }
}

private fun <T : DataModel<*>> FilterResolver<T>.forEach(
    type: FilterType,
    keys: Array<SurrogateKey>?,
    predicate: ((T) -> Boolean)?,
    action: (UniqueId) -> Unit
) {
    resolveKeys(type)
        .toSet()
        .asSequence()
        .filter { id ->
            if (keys.isNullOrEmpty()) true
            else keys.any { id.tags.contains(it) }
        }
        .filter { id ->
            if (predicate == null) true
            else resolveValue(type, id)?.let(predicate) ?: false
        }
        .forEach(action)
}
