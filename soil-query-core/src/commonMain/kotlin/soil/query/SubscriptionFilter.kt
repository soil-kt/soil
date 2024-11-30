// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.FilterResolver
import soil.query.core.FilterType
import soil.query.core.RemoveFilter
import soil.query.core.ResumeFilter
import soil.query.core.UniqueId

/**
 * Filter for removing subscriptions.
 */
typealias RemoveSubscriptionsFilter = RemoveFilter<SubscriptionModel<*>>

/**
 * Filter for resuming subscriptions.
 */
typealias ResumeSubscriptionsFilter = ResumeFilter<SubscriptionModel<*>>

/**
 * Filter resolver for subscriptions.
 */
internal class SubscriptionFilterResolver(
    private val store: Map<UniqueId, Subscription<*>>,
    private val cache: SubscriptionCache
) : FilterResolver<SubscriptionModel<*>> {

    override fun resolveKeys(type: FilterType): Set<UniqueId> = when (type) {
        FilterType.Active -> store.keys
        FilterType.Inactive -> cache.keys
    }

    override fun resolveValue(type: FilterType, id: UniqueId): SubscriptionModel<*>? = when (type) {
        FilterType.Active -> store[id]?.state?.value
        FilterType.Inactive -> cache[id]
    }
}
