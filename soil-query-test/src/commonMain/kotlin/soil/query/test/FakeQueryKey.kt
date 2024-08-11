// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import soil.query.QueryKey
import soil.query.QueryReceiver

/**
 * Creates a fake query key that returns the result of the given [mock] function.
 */
class FakeQueryKey<T>(
    private val target: QueryKey<T>,
    private val mock: FakeQueryFetch<T>
) : QueryKey<T> by target {
    override val fetch: suspend QueryReceiver.() -> T = { mock() }
}

typealias FakeQueryFetch<T> = suspend () -> T
