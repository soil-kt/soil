// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch

/**
 * State for managing the execution result of [Subscription].
 */
data class SubscriptionState<T> internal constructor(
    override val reply: Reply<T> = Reply.None,
    override val replyUpdatedAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val status: SubscriptionStatus = SubscriptionStatus.Pending,
    override val subscriberStatus: SubscriberStatus = SubscriberStatus.NoSubscribers
) : SubscriptionModel<T> {

    companion object {

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Pending] status.
         *
         * @param subscriberStatus The status of the subscriber.
         */
        fun <T> initial(
            subscriberStatus: SubscriberStatus = SubscriberStatus.NoSubscribers
        ): SubscriptionState<T> {
            return SubscriptionState(
                subscriberStatus = subscriberStatus
            )
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Success] status.
         *
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param subscriberStatus The status of the subscriber.
         */
        fun <T> success(
            data: T,
            dataUpdatedAt: Long = epoch(),
            subscriberStatus: SubscriberStatus = SubscriberStatus.NoSubscribers
        ): SubscriptionState<T> {
            return SubscriptionState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                status = SubscriptionStatus.Success,
                subscriberStatus = subscriberStatus
            )
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Failure] status.
         *
         * @param error The error to be stored in the state.
         * @param errorUpdatedAt The timestamp when the error was updated. Default is the current epoch.
         * @param subscriberStatus The status of the subscriber.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch(),
            subscriberStatus: SubscriberStatus = SubscriberStatus.NoSubscribers
        ): SubscriptionState<T> {
            return SubscriptionState(
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = SubscriptionStatus.Failure,
                subscriberStatus = subscriberStatus
            )
        }

        /**
         * Creates a new [SubscriptionState] with the [SubscriptionStatus.Failure] status.
         *
         * @param error The error to be stored in the state.
         * @param errorUpdatedAt The timestamp when the error was updated. Default is the current epoch.
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param subscriberStatus The status of the subscriber.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch(),
            data: T,
            dataUpdatedAt: Long = epoch(),
            subscriberStatus: SubscriberStatus = SubscriberStatus.NoSubscribers
        ): SubscriptionState<T> {
            return SubscriptionState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = SubscriptionStatus.Failure,
                subscriberStatus = subscriberStatus
            )
        }
    }
}
