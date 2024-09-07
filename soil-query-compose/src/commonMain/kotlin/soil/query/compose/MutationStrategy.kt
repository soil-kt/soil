// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import soil.query.MutationRef
import soil.query.MutationState

/**
 * A mechanism to finely adjust the behavior of the mutation on a component basis in Composable functions.
 *
 * If you want to customize, please create a class implementing [MutationStrategy].
 * For example, this is useful when you want to switch your implementation to `collectAsStateWithLifecycle`.
 */
@Stable
interface MutationStrategy {

    @Composable
    fun <T, S> collectAsState(mutation: MutationRef<T, S>): MutationState<T>

    companion object
}

/**
 * The default built-in strategy for Mutation built into the library.
 */
val MutationStrategy.Companion.Default: MutationStrategy
    get() = MutationStrategyDefault

private object MutationStrategyDefault : MutationStrategy {
    @Composable
    override fun <T, S> collectAsState(mutation: MutationRef<T, S>): MutationState<T> {
        return mutation.state.collectAsState().value
    }
}
