// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply

/**
 * Subscription actions are used to update the [subscription state][SubscriptionState].
 *
 * @param T Type of the receive value from the subscription.
 */
sealed interface SubscriptionAction<out T> {

    /**
     * Resets the subscription state.
     */
    data object Reset : SubscriptionAction<Nothing>

    /**
     * Indicates that the subscription is successful.
     *
     * @param data The data to be updated.
     * @param dataUpdatedAt The timestamp when the data was updated.
     */
    data class ReceiveSuccess<T>(
        val data: T,
        val dataUpdatedAt: Long
    ) : SubscriptionAction<T>

    /**
     * Indicates that the subscription has failed.
     *
     * @param error The error that occurred.
     * @param errorUpdatedAt The timestamp when the error occurred.
     */
    data class ReceiveFailure(
        val error: Throwable,
        val errorUpdatedAt: Long
    ) : SubscriptionAction<Nothing>
}

typealias SubscriptionReducer<T> = (SubscriptionState<T>, SubscriptionAction<T>) -> SubscriptionState<T>
typealias SubscriptionDispatch<T> = (SubscriptionAction<T>) -> Unit

/**
 * Creates a [SubscriptionReducer] function.
 */
fun <T> createSubscriptionReducer(): SubscriptionReducer<T> = { state, action ->
    when (action) {
        is SubscriptionAction.Reset -> {
            state.copy(
                reply = Reply.none(),
                replyUpdatedAt = 0,
                error = null,
                errorUpdatedAt = 0,
                status = SubscriptionStatus.Pending
            )
        }

        is SubscriptionAction.ReceiveSuccess -> {
            state.copy(
                status = SubscriptionStatus.Success,
                reply = Reply(action.data),
                replyUpdatedAt = action.dataUpdatedAt,
                error = null,
                errorUpdatedAt = if (state.error != null) action.dataUpdatedAt else state.errorUpdatedAt
            )
        }

        is SubscriptionAction.ReceiveFailure -> {
            state.copy(
                status = SubscriptionStatus.Failure,
                error = action.error,
                errorUpdatedAt = action.errorUpdatedAt
            )
        }
    }
}
