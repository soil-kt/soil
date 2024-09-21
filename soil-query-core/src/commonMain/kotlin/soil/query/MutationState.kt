// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch
import kotlin.jvm.JvmInline

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

    /**
     * Returns a new [MutationState] with the items included in [keys] omitted from the current [MutationState].
     *
     * NOTE: This function is provided to optimize recomposition for Compose APIs.
     */
    fun omit(keys: Set<OmitKey>): MutationState<T> {
        if (keys.isEmpty()) return this
        return copy(
            replyUpdatedAt = if (keys.contains(OmitKey.replyUpdatedAt)) 0 else replyUpdatedAt,
            errorUpdatedAt = if (keys.contains(OmitKey.errorUpdatedAt)) 0 else errorUpdatedAt,
            mutatedCount = if (keys.contains(OmitKey.mutatedCount)) 0 else mutatedCount
        )
    }

    @JvmInline
    value class OmitKey(val name: String) {
        companion object {
            val replyUpdatedAt = OmitKey("replyUpdatedAt")
            val errorUpdatedAt = OmitKey("errorUpdatedAt")
            val mutatedCount = OmitKey("mutatedCount")
        }
    }

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

        /**
         * Creates a new [MutationState] for Testing.
         *
         * NOTE: **This method is for testing purposes only.**
         */
        fun <T> test(
            reply: Reply<T> = Reply.None,
            replyUpdatedAt: Long = 0,
            error: Throwable? = null,
            errorUpdatedAt: Long = 0,
            status: MutationStatus = MutationStatus.Idle,
            mutatedCount: Int = 0
        ): MutationState<T> {
            return MutationState(
                reply = reply,
                replyUpdatedAt = replyUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = status,
                mutatedCount = mutatedCount
            )
        }
    }
}
