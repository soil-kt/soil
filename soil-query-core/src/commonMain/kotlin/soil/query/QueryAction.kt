// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

sealed interface QueryAction<out T> {

    data class Fetching(
        val isInvalidated: Boolean? = null
    ) : QueryAction<Nothing>

    data class FetchSuccess<T>(
        val data: T,
        val dataUpdatedAt: Long,
        val dataStaleAt: Long
    ) : QueryAction<T>

    data class FetchFailure(
        val error: Throwable,
        val errorUpdatedAt: Long,
        val paused: QueryFetchStatus.Paused? = null
    ) : QueryAction<Nothing>

    data object Invalidate : QueryAction<Nothing>

    data class ForceUpdate<T>(
        val data: T,
        val dataUpdatedAt: Long
    ) : QueryAction<T>
}

typealias QueryReducer<T> = (QueryState<T>, QueryAction<T>) -> QueryState<T>
typealias QueryDispatch<T> = (QueryAction<T>) -> Unit

fun <T> createQueryReducer(): QueryReducer<T> = { state, action ->
    when (action) {
        is QueryAction.Fetching -> {
            state.copy(
                fetchStatus = QueryFetchStatus.Fetching,
                isInvalidated = action.isInvalidated ?: state.isInvalidated
            )
        }

        is QueryAction.FetchSuccess -> {
            state.copy(
                data = action.data,
                dataUpdatedAt = action.dataUpdatedAt,
                dataStaleAt = action.dataStaleAt,
                error = null,
                errorUpdatedAt = action.dataUpdatedAt,
                status = QueryStatus.Success,
                fetchStatus = QueryFetchStatus.Idle,
                isInvalidated = false,
                isPlaceholderData = false
            )
        }

        is QueryAction.FetchFailure -> {
            state.copy(
                error = action.error,
                errorUpdatedAt = action.errorUpdatedAt,
                status = QueryStatus.Failure,
                fetchStatus = action.paused ?: QueryFetchStatus.Idle
            )
        }

        is QueryAction.Invalidate -> {
            state.copy(
                isInvalidated = true
            )
        }

        is QueryAction.ForceUpdate -> {
            state.copy(
                data = action.data,
                dataUpdatedAt = action.dataUpdatedAt
            )
        }
    }
}
