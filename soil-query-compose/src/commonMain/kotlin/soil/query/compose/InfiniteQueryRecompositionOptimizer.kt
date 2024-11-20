// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.QueryChunks
import soil.query.QueryState
import soil.query.QueryStatus

/**
 * A recomposition optimizer for [QueryState] with [QueryChunks].
 */
interface InfiniteQueryRecompositionOptimizer {

    /**
     * Omit the specified keys from the [QueryState] with [QueryChunks].
     *
     * @param state The infinite query state.
     * @return The optimized infinite query state.
     */
    fun <T, S> omit(state: QueryState<QueryChunks<T, S>>): QueryState<QueryChunks<T, S>>

    companion object
}

/**
 * Optimizer implementation for [InfiniteQueryStrategy.Companion.Default].
 */
val InfiniteQueryRecompositionOptimizer.Companion.Enabled: InfiniteQueryRecompositionOptimizer
    get() = InfiniteQueryRecompositionOptimizerEnabled

private object InfiniteQueryRecompositionOptimizerEnabled : InfiniteQueryRecompositionOptimizer {
    override fun <T, S> omit(state: QueryState<QueryChunks<T, S>>): QueryState<QueryChunks<T, S>> {
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
val InfiniteQueryRecompositionOptimizer.Companion.Disabled: InfiniteQueryRecompositionOptimizer
    get() = DisabledInfiniteQueryRecompositionOptimizer

private object DisabledInfiniteQueryRecompositionOptimizer : InfiniteQueryRecompositionOptimizer {
    override fun <T, S> omit(state: QueryState<QueryChunks<T, S>>): QueryState<QueryChunks<T, S>> {
        return state
    }
}
