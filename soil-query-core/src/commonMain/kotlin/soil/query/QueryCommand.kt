// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.ErrorRecord
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.RetryCallback
import soil.query.core.RetryFn
import soil.query.core.UniqueId
import soil.query.core.epoch
import soil.query.core.exponentialBackOff
import soil.query.core.toEpoch
import soil.query.core.vvv
import kotlin.coroutines.cancellation.CancellationException

/**
 * Query command to handle query.
 *
 * @param T Type of the return value from the query.
 */
interface QueryCommand<T> {

    /**
     * Handles the query.
     */
    suspend fun handle(ctx: Context<T>)

    /**
     * Context for query command.
     *
     * @param T Type of the return value from the query.
     */
    interface Context<T> {
        val receiver: QueryReceiver
        val options: QueryOptions
        val state: QueryModel<T>
        val dispatch: QueryDispatch<T>
        val relay: QueryErrorRelay?
    }
}

internal typealias QueryErrorRelay = (ErrorRecord) -> Unit

/**
 * Determines whether a fetch operation is necessary based on the current state.
 *
 * @return `true` if fetch operation is allowed, `false` otherwise.
 */
fun <T> QueryCommand.Context<T>.shouldFetch(revision: String? = null): Boolean {
    if (revision != null && revision != state.revision) {
        return false
    }
    if (state.isFailure && state.isPaused()) {
        return false
    }
    return state.isInvalidated
        || state.isPending
        || state.isStaled()
}

/**
 * Determines whether query processing needs to be paused based on [error].
 *
 * Use [QueryOptions.pauseDurationAfter] for cases like HTTP 503 errors, where requests from the client need to be stopped for a certain period.
 * There's currently no mechanism for automatic resumption after the specified duration.
 * Resuming will require implementation on the UI, such as a retry button.
 *
 * @return Returns [QueryFetchStatus.Paused] if pausing is necessary, otherwise returns `null`.
 */
fun <T> QueryCommand.Context<T>.shouldPause(error: Throwable): QueryFetchStatus.Paused? {
    val pauseTime = options.pauseDurationAfter?.invoke(error)
    if (pauseTime != null && pauseTime.isPositive()) {
        return QueryFetchStatus.Paused(unpauseAt = pauseTime.toEpoch())
    }
    return null
}

/**
 * Fetches the data.
 *
 * @param key Instance of a class implementing [QueryKey].
 * @param retryFn The retry function.
 * @return The result of the fetch.
 */
suspend fun <T> QueryCommand.Context<T>.fetch(
    key: QueryKey<T>,
    retryFn: RetryFn<T> = options.exponentialBackOff(onRetry = onRetryCallback(key.id))
): Result<T> {
    return try {
        val value = retryFn.withRetry { with(key) { receiver.fetch() } }
        Result.success(value)
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

/**
 * Dispatches the fetch result.
 *
 * @param key Instance of a class implementing [QueryKey].
 * @param marker The marker with additional information based on the caller of a query.
 * @param callback The callback to receive the result of the query.
 */
suspend inline fun <T> QueryCommand.Context<T>.dispatchFetchResult(
    key: QueryKey<T>,
    marker: Marker,
    noinline callback: QueryCallback<T>?
) {
    fetch(key)
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess { dispatchFetchSuccess(it, key.contentEquals) }
        .onFailure(::dispatchFetchFailure)
        .onFailure { reportQueryError(it, key.id, marker) }
        .also { callback?.invoke(it) }
}

/**
 * Dispatches the fetch success.
 *
 * @param data The fetched data.
 */
fun <T> QueryCommand.Context<T>.dispatchFetchSuccess(
    data: T,
    contentEquals: QueryContentEquals<T>? = null
) {
    val currentAt = epoch()
    val currentReply = state.reply
    val action = if (currentReply is Reply.Some && contentEquals?.invoke(currentReply.value, data) == true) {
        QueryAction.FetchSuccess(
            data = currentReply.value,
            dataUpdatedAt = state.replyUpdatedAt,
            dataStaleAt = options.staleTime.toEpoch(currentAt)
        )
    } else {
        QueryAction.FetchSuccess(
            data = data,
            dataUpdatedAt = currentAt,
            dataStaleAt = options.staleTime.toEpoch(currentAt)
        )
    }
    dispatch(action)
}

/**
 * Dispatches the fetch failure.
 *
 * @param error The fetch error.
 */
fun <T> QueryCommand.Context<T>.dispatchFetchFailure(error: Throwable) {
    val currentAt = epoch()
    val currentError = state.error
    val action = if (currentError != null && options.errorEquals?.invoke(currentError, error) == true) {
        QueryAction.FetchFailure(
            error = currentError,
            errorUpdatedAt = state.errorUpdatedAt,
            paused = shouldPause(currentError)
        )
    } else {
        QueryAction.FetchFailure(
            error = error,
            errorUpdatedAt = currentAt,
            paused = shouldPause(error)
        )
    }
    dispatch(action)
}

/**
 * Reports the query error.
 *
 * @param error The query error.
 * @param id The unique identifier of the query.
 * @param marker The marker for the query.
 */
fun <T> QueryCommand.Context<T>.reportQueryError(error: Throwable, id: UniqueId, marker: Marker) {
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

internal fun <T> QueryCommand.Context<T>.onRetryCallback(
    id: UniqueId,
): RetryCallback? {
    options.logger ?: return null
    return { err, count, nextBackOff ->
        options.vvv(id) { "retry(count=$count next=$nextBackOff error=${err.message})" }
    }
}
