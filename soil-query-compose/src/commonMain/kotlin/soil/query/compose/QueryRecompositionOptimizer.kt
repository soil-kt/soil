// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.QueryState
import soil.query.QueryStatus

/**
 * A recomposition optimizer for [QueryState].
 */
interface QueryRecompositionOptimizer {

    /**
     * Omit the specified keys from the [QueryState].
     *
     * @param state The query state.
     * @return The optimized query state.
     */
    fun <T> omit(state: QueryState<T>): QueryState<T>

    companion object
}

/**
 * Optimizer implementation for [QueryStrategy.Companion.Default].
 */
val QueryRecompositionOptimizer.Companion.Enabled: QueryRecompositionOptimizer
    get() = QueryRecompositionOptimizerEnabled

private object QueryRecompositionOptimizerEnabled : QueryRecompositionOptimizer {
    override fun <T> omit(state: QueryState<T>): QueryState<T> {
        val keys = buildSet {
            add(QueryState.OmitKey.replyUpdatedAt)
            add(QueryState.OmitKey.staleAt)
            when (state.status) {
                QueryStatus.Pending -> {
                    add(QueryState.OmitKey.errorUpdatedAt)
                    add(QueryState.OmitKey.fetchStatus)
                }

                QueryStatus.Success -> {
                    add(QueryState.OmitKey.errorUpdatedAt)
                    if (!state.isInvalidated) {
                        add(QueryState.OmitKey.fetchStatus)
                    }
                }

                QueryStatus.Failure -> {
                    if (!state.isInvalidated) {
                        add(QueryState.OmitKey.fetchStatus)
                    }
                }
            }
        }
        return state.omit(keys)
    }
}

/**
 * Option that performs no optimization.
 */
val QueryRecompositionOptimizer.Companion.Disabled: QueryRecompositionOptimizer
    get() = DisabledQueryRecompositionOptimizer

private object DisabledQueryRecompositionOptimizer : QueryRecompositionOptimizer {
    override fun <T> omit(state: QueryState<T>): QueryState<T> {
        return state
    }
}
