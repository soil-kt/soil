// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import soil.query.InfiniteQueryKey
import soil.query.QueryReceiver

/**
 * Creates a fake infinite query key that returns the result of the given [mock] function.
 */
class FakeInfiniteQueryKey<T, S>(
    private val target: InfiniteQueryKey<T, S>,
    private val mock: FakeInfiniteQueryFetch<T, S>
) : InfiniteQueryKey<T, S> by target {
    override val fetch: suspend QueryReceiver.(param: S) -> T = { mock(it) }
}

typealias FakeInfiniteQueryFetch<T, S> = suspend (param: S) -> T
