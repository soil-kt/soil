// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.SubscriptionState
import soil.query.SubscriptionStatus

/**
 * A recomposition optimizer for [SubscriptionState].
 */
interface SubscriptionRecompositionOptimizer {

    /**
     * Omit the specified keys from the [SubscriptionState].
     *
     * @param state The subscription state.
     * @return The optimized subscription state.
     */
    fun <T> omit(state: SubscriptionState<T>): SubscriptionState<T>

    companion object
}

/**
 * Optimizer implementation for [SubscriptionStrategy.Companion.Default].
 */
val SubscriptionRecompositionOptimizer.Companion.Enabled: SubscriptionRecompositionOptimizer
    get() = SubscriptionRecompositionOptimizerEnabled

private object SubscriptionRecompositionOptimizerEnabled : SubscriptionRecompositionOptimizer {
    override fun <T> omit(state: SubscriptionState<T>): SubscriptionState<T> {
        val keys = buildSet {
            add(SubscriptionState.OmitKey.replyUpdatedAt)
            add(SubscriptionState.OmitKey.restartedAt)
            when (state.status) {
                SubscriptionStatus.Pending -> {
                    add(SubscriptionState.OmitKey.errorUpdatedAt)
                }

                SubscriptionStatus.Success -> {
                    add(SubscriptionState.OmitKey.errorUpdatedAt)
                }

                SubscriptionStatus.Failure -> Unit
            }
        }
        return state.omit(keys)
    }
}

/**
 * Option that performs no optimization.
 */
val SubscriptionRecompositionOptimizer.Companion.Disabled: SubscriptionRecompositionOptimizer
    get() = DisabledSubscriptionRecompositionOptimizer

private object DisabledSubscriptionRecompositionOptimizer : SubscriptionRecompositionOptimizer {
    override fun <T> omit(state: SubscriptionState<T>): SubscriptionState<T> {
        return state
    }
}
