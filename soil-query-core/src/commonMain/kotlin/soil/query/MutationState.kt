// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch

/**
 * State for managing the execution result of [Mutation].
 */
data class MutationState<T> internal constructor(
    override val reply: Reply<T> = Reply.None,
    override val replyUpdatedAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val status: MutationStatus = MutationStatus.Idle,
    override val mutatedCount: Int = 0
) : MutationModel<T> {
    companion object {

        /**
         * Creates a new [MutationState] with the [MutationStatus.Idle] status.
         */
        fun <T> initial(): MutationState<T> {
            return MutationState()
        }

        /**
         * Creates a new [MutationState] with the [MutationStatus.Pending] status.
         */
        fun <T> pending(): MutationState<T> {
            return MutationState(
                status = MutationStatus.Pending
            )
        }

        /**
         * Creates a new [MutationState] with the [MutationStatus.Success] status.
         *
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param mutatedCount The number of times the data was mutated.
         */
        fun <T> success(
            data: T,
            dataUpdatedAt: Long = epoch(),
            mutatedCount: Int = 1
        ): MutationState<T> {
            return MutationState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                status = MutationStatus.Success,
                mutatedCount = mutatedCount
            )
        }

        /**
         * Creates a new [MutationState] with the [MutationStatus.Failure] status.
         *
         * @param error The error that occurred.
         * @param errorUpdatedAt The timestamp when the error occurred. Default is the current epoch.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch()
        ): MutationState<T> {
            return MutationState(
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = MutationStatus.Failure
            )
        }

        /**
         * Creates a new [MutationState] with the [MutationStatus.Failure] status.
         *
         * @param error The error that occurred.
         * @param errorUpdatedAt The timestamp when the error occurred. Default is the current epoch.
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param mutatedCount The number of times the data was mutated.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch(),
            data: T,
            dataUpdatedAt: Long = epoch(),
            mutatedCount: Int = 1
        ): MutationState<T> {
            return MutationState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = MutationStatus.Failure,
                mutatedCount = mutatedCount
            )
        }
    }
}
