// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.epoch

/**
 * Mutation actions are used to update the [mutation state][MutationState].
 *
 * @param T Type of the return value from the mutation.
 */
sealed interface MutationAction<out T> {

    /**
     * Resets the mutation state.
     */
    data object Reset : MutationAction<Nothing>

    /**
     * Indicates that the mutation is in progress.
     */
    data object Mutating : MutationAction<Nothing>

    /**
     * Indicates that the mutation is successful.
     *
     * @param data The data to be updated.
     * @param dataUpdatedAt The timestamp when the data was updated.
     */
    data class MutateSuccess<T>(
        val data: T,
        val dataUpdatedAt: Long? = null
    ) : MutationAction<T>

    /**
     * Indicates that the mutation has failed.
     *
     * @param error The error that occurred.
     * @param errorUpdatedAt The timestamp when the error occurred.
     */
    data class MutateFailure(
        val error: Throwable,
        val errorUpdatedAt: Long? = null
    ) : MutationAction<Nothing>
}

typealias MutationReducer<T> = (MutationState<T>, MutationAction<T>) -> MutationState<T>
typealias MutationDispatch<T> = (MutationAction<T>) -> Unit

/**
 * Creates a [MutationReducer] function.
 */
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
