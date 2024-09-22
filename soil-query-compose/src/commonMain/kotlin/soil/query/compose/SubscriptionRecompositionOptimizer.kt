// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.SubscriberStatus
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
val SubscriptionRecompositionOptimizer.Companion.Default: SubscriptionRecompositionOptimizer
    get() = DefaultSubscriptionRecompositionOptimizer

private object DefaultSubscriptionRecompositionOptimizer :
    AbstractSubscriptionRecompositionOptimizer(SubscriberStatus.Active)

/**
 * Optimizer implementation for [SubscriptionStrategy.Companion.Lazy].
 */
val SubscriptionRecompositionOptimizer.Companion.Lazy: SubscriptionRecompositionOptimizer
    get() = LazySubscriptionRecompositionOptimizer

private object LazySubscriptionRecompositionOptimizer :
    AbstractSubscriptionRecompositionOptimizer()

private abstract class AbstractSubscriptionRecompositionOptimizer(
    private val subscriberStatus: SubscriberStatus? = null
) : SubscriptionRecompositionOptimizer {
    override fun <T> omit(state: SubscriptionState<T>): SubscriptionState<T> {
        val keys = buildSet {
            add(SubscriptionState.OmitKey.replyUpdatedAt)
            if (subscriberStatus != null) {
                add(SubscriptionState.OmitKey.subscriberStatus)
            }
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
        return state.omit(keys, subscriberStatus ?: SubscriberStatus.NoSubscribers)
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
