// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.RetryCallback
import soil.query.internal.RetryFn
import soil.query.internal.UniqueId
import soil.query.internal.epoch
import soil.query.internal.exponentialBackOff
import soil.query.internal.toEpoch
import soil.query.internal.vvv
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
    }
}

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
        || state.isPlaceholderData
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
 */
suspend inline fun <T> QueryCommand.Context<T>.dispatchFetchResult(
    key: QueryKey<T>,
    noinline callback: QueryCallback<T>? = null
) {
    fetch(key)
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess(::dispatchFetchSuccess)
        .onFailure(::dispatchFetchFailure)
        .onFailure { options.onError?.invoke(it, state, key.id) }
        .also { callback?.invoke(it) }
}

/**
 * Dispatches the fetch success.
 *
 * @param data The fetched data.
 */
fun <T> QueryCommand.Context<T>.dispatchFetchSuccess(data: T) {
    val currentAt = epoch()
    val action = QueryAction.FetchSuccess(
        data = data,
        dataUpdatedAt = currentAt,
        dataStaleAt = options.staleTime.toEpoch(currentAt)
    )
    dispatch(action)
}

/**
 * Dispatches the fetch failure.
 *
 * @param error The fetch error.
 */
fun <T> QueryCommand.Context<T>.dispatchFetchFailure(error: Throwable) {
    val currentAt = epoch()
    val action = QueryAction.FetchFailure(
        error = error,
        errorUpdatedAt = currentAt,
        paused = shouldPause(error)
    )
    dispatch(action)
}

internal fun <T> QueryCommand.Context<T>.onRetryCallback(
    id: UniqueId,
): RetryCallback? {
    options.logger ?: return null
    return { err, count, nextBackOff ->
        options.vvv(id) { "retry(count=$count next=$nextBackOff error=${err.message})" }
    }
}
