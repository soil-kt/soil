// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch
import kotlin.jvm.JvmInline

/**
 * State for managing the execution result of [Subscription].
 */
data class SubscriptionState<T> internal constructor(
    override val reply: Reply<T> = Reply.None,
    override val replyUpdatedAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val restartedAt: Long = 0,
    override val status: SubscriptionStatus = SubscriptionStatus.Pending
) : SubscriptionModel<T> {

    /**
     * Workaround:
     * The following warning appeared when updating the [reply] property within [SwrCachePlusInternal.setQueryData],
     * so I replaced the update process with a method that includes type information.
     * ref. https://youtrack.jetbrains.com/issue/KT-49404
     */
    internal fun patch(
        data: T,
        dataUpdatedAt: Long = epoch()
    ): SubscriptionState<T> = copy(
        reply = Reply(data),
        replyUpdatedAt = dataUpdatedAt
    )

    /**
     * Returns a new [SubscriptionState] with the items included in [keys] omitted from the current [SubscriptionState].
     *
     * NOTE: This function is provided to optimize recomposition for Compose APIs.
     */
    fun omit(
        keys: Set<OmitKey>
    ): SubscriptionState<T> {
        if (keys.isEmpty()) return this
        return copy(
            replyUpdatedAt = if (keys.contains(OmitKey.replyUpdatedAt)) 0 else replyUpdatedAt,
            errorUpdatedAt = if (keys.contains(OmitKey.errorUpdatedAt)) 0 else errorUpdatedAt,
            restartedAt = if (keys.contains(OmitKey.restartedAt)) 0 else restartedAt
        )
    }

    @JvmInline
    value class OmitKey(val name: String) {
        companion object {
            val replyUpdatedAt = OmitKey("replyUpdatedAt")
            val errorUpdatedAt = OmitKey("errorUpdatedAt")
            val restartedAt = OmitKey("restartedAt")
        }
    }

    companion object {

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Pending] status.
         */
        fun <T> initial(): SubscriptionState<T> {
            return SubscriptionState()
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Success] status.
         *
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         */
        fun <T> success(
            data: T,
            dataUpdatedAt: Long = epoch()
        ): SubscriptionState<T> {
            return SubscriptionState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                status = SubscriptionStatus.Success
            )
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Failure] status.
         *
         * @param error The error to be stored in the state.
         * @param errorUpdatedAt The timestamp when the error was updated. Default is the current epoch.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch()
        ): SubscriptionState<T> {
            return SubscriptionState(
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = SubscriptionStatus.Failure
            )
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Failure] status.
         *
         * @param error The error to be stored in the state.
         * @param errorUpdatedAt The timestamp when the error was updated. Default is the current epoch.
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch(),
            data: T,
            dataUpdatedAt: Long = epoch()
        ): SubscriptionState<T> {
            return SubscriptionState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = SubscriptionStatus.Failure
            )
        }

        /**
         * Creates a new [SubscriptionState] for Testing.
         *
         * NOTE: **This method is for testing purposes only.**
         */
        fun <T> test(
            reply: Reply<T> = Reply.None,
            replyUpdatedAt: Long = 0,
            error: Throwable? = null,
            errorUpdatedAt: Long = 0,
            restartedAt: Long = 0,
            status: SubscriptionStatus = SubscriptionStatus.Pending
        ): SubscriptionState<T> {
            return SubscriptionState(
                reply = reply,
                replyUpdatedAt = replyUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                restartedAt = restartedAt,
                status = status
            )
        }
    }
}
