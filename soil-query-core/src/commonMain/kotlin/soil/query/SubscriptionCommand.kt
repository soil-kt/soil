// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ErrorRecord
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.UniqueId
import soil.query.core.epoch

/**
 * Subscription command to handle subscription.
 *
 * @param T Type of the receive value from the subscription.
 */
interface SubscriptionCommand<T> {

    /**
     * Handles the subscription.
     */
    suspend fun handle(ctx: Context<T>)

    /**
     * Context for subscription command.
     *
     * @param T Type of the receive value from the subscription.
     */
    interface Context<T> {
        val options: SubscriptionOptions
        val state: SubscriptionModel<T>
        val dispatch: SubscriptionDispatch<T>
        val restart: SubscriptionRestart
        val relay: SubscriptionErrorRelay?
    }
}

internal typealias SubscriptionErrorRelay = (ErrorRecord) -> Unit
internal typealias SubscriptionRestart = () -> Unit

/**
 * Dispatches the result of the subscription.
 *
 * @param key The subscription key.
 * @param result The result of the subscription.
 * @param marker The marker for the subscription.
 */
fun <T> SubscriptionCommand.Context<T>.dispatchResult(
    key: SubscriptionKey<T>,
    result: Result<T>,
    marker: Marker
) {
    result
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess { dispatchReceiveSuccess(it, key.contentEquals) }
        .onFailure(::dispatchReceiveFailure)
        .onFailure { reportSubscriptionError(it, key.id, marker) }
}

/**
 * Dispatches the success result of the subscription.
 *
 * @param data The data of the subscription.
 */
fun <T> SubscriptionCommand.Context<T>.dispatchReceiveSuccess(
    data: T,
    contentEquals: SubscriptionContentEquals<T>? = null
) {
    val currentAt = epoch()
    val currentReply = state.reply
    val action = if (currentReply is Reply.Some && contentEquals?.invoke(currentReply.value, data) == true) {
        SubscriptionAction.ReceiveSuccess(
            data = currentReply.value,
            dataUpdatedAt = state.replyUpdatedAt
        )
    } else {
        SubscriptionAction.ReceiveSuccess(
            data = data,
            dataUpdatedAt = currentAt
        )
    }
    dispatch(action)
}

/**
 * Dispatches the failure result of the subscription.
 *
 * @param error The error of the subscription.
 */
fun <T> SubscriptionCommand.Context<T>.dispatchReceiveFailure(error: Throwable) {
    val currentAt = epoch()
    val currentError = state.error
    val action = if (currentError != null && options.errorEquals?.invoke(currentError, error) == true) {
        SubscriptionAction.ReceiveFailure(
            error = currentError,
            errorUpdatedAt = state.errorUpdatedAt
        )
    } else {
        SubscriptionAction.ReceiveFailure(
            error = error,
            errorUpdatedAt = currentAt
        )
    }
    dispatch(action)
}

/**
 * Reports the subscription error.
 *
 * @param error The error of the subscription.
 * @param id The unique identifier of the subscription.
 * @param marker The marker for the subscription.
 */
fun <T> SubscriptionCommand.Context<T>.reportSubscriptionError(error: Throwable, id: UniqueId, marker: Marker) {
    if (options.onError == null && relay == null) {
        return
    }
    val record = ErrorRecord(error, id, marker)
    options.onError?.invoke(record, state)
    val errorRelay = relay
    if (errorRelay != null && options.shouldSuppressErrorRelay?.invoke(record, state) != true) {
        errorRelay(record)
    }
}
