// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.annotation.InternalSoilQueryApi
import soil.query.core.UniqueId

@InternalSoilQueryApi
interface SwrCacheView {
    val mutationStoreView: Map<UniqueId, Mutation<*>>
    val queryStoreView: Map<UniqueId, Query<*>>
}

@InternalSoilQueryApi
interface SwrCachePlusView : SwrCacheView {
    val subscriptionStoreView: Map<UniqueId, Subscription<*>>
}
