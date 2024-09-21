// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.MutationState
import soil.query.MutationStatus

/**
 * A recomposition optimizer for [MutationState].
 */
interface MutationRecompositionOptimizer {

    /**
     * Omit the specified keys from the [MutationState].
     *
     * @param state The mutation state.
     * @return The optimized mutation state.
     */
    fun <T> omit(state: MutationState<T>): MutationState<T>

    companion object
}

/**
 * Optimizer implementation for [MutationStrategy.Companion.Default].
 */
val MutationRecompositionOptimizer.Companion.Default: MutationRecompositionOptimizer
    get() = DefaultMutationRecompositionOptimizer

private object DefaultMutationRecompositionOptimizer : MutationRecompositionOptimizer {
    override fun <T> omit(state: MutationState<T>): MutationState<T> {
        val keys = buildSet {
            add(MutationState.OmitKey.replyUpdatedAt)
            add(MutationState.OmitKey.mutatedCount)
            when (state.status) {
                MutationStatus.Idle -> {
                    add(MutationState.OmitKey.errorUpdatedAt)
                }

                MutationStatus.Pending -> {
                    if (state.error == null) {
                        add(MutationState.OmitKey.errorUpdatedAt)
                    }
                }

                MutationStatus.Success -> {
                    add(MutationState.OmitKey.errorUpdatedAt)
                }

                MutationStatus.Failure -> Unit
            }
        }
        return state.omit(keys)
    }
}

/**
 * Option that performs no optimization.
 */
val MutationRecompositionOptimizer.Companion.Disabled: MutationRecompositionOptimizer
    get() = DisabledMutationRecompositionOptimizer

private object DisabledMutationRecompositionOptimizer : MutationRecompositionOptimizer {
    override fun <T> omit(state: MutationState<T>): MutationState<T> {
        return state
    }
}
