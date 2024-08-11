// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import soil.query.MutationKey
import soil.query.MutationReceiver

/**
 * Creates a fake mutation key that returns the result of the given [mock] function.
 */
class FakeMutationKey<T, S>(
    private val target: MutationKey<T, S>,
    private val mock: FakeMutationMutate<T, S>
) : MutationKey<T, S> by target {
    override val mutate: suspend MutationReceiver.(variable: S) -> T = { mock(it) }
}

typealias FakeMutationMutate<T, S> = suspend (variable: S) -> T
