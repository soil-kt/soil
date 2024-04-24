// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.epoch

sealed interface MutationAction<out T> {

    data object Reset : MutationAction<Nothing>

    data object Mutating : MutationAction<Nothing>

    data class MutateSuccess<T>(
        val data: T,
        val dataUpdatedAt: Long? = null
    ) : MutationAction<T>

    data class MutateFailure(
        val error: Throwable,
        val errorUpdatedAt: Long? = null
    ) : MutationAction<Nothing>
}

typealias MutationReducer<T> = (MutationState<T>, MutationAction<T>) -> MutationState<T>
typealias MutationDispatch<T> = (MutationAction<T>) -> Unit

fun <T> createMutationReducer(): MutationReducer<T> = { state, action ->
    when (action) {
        is MutationAction.Reset -> {
            state.copy(
                data = null,
                dataUpdatedAt = 0,
                error = null,
                errorUpdatedAt = 0,
                status = MutationStatus.Idle,
                mutatedCount = 0
            )
        }

        is MutationAction.Mutating -> {
            state.copy(
                status = MutationStatus.Pending,
            )
        }

        is MutationAction.MutateSuccess -> {
            val updatedAt = action.dataUpdatedAt ?: epoch()
            state.copy(
                status = MutationStatus.Success,
                data = action.data,
                dataUpdatedAt = updatedAt,
                error = null,
                errorUpdatedAt = updatedAt,
                mutatedCount = state.mutatedCount + 1
            )
        }

        is MutationAction.MutateFailure -> {
            state.copy(
                status = MutationStatus.Failure,
                error = action.error,
                errorUpdatedAt = action.errorUpdatedAt ?: epoch()
            )
        }
    }
}
