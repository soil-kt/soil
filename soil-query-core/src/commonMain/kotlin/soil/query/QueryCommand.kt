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

interface QueryCommand<T> {

    suspend fun handle(ctx: Context<T>)

    interface Context<T> {
        val receiver: QueryReceiver
        val options: QueryOptions
        val state: QueryState<T>
        val dispatch: QueryDispatch<T>
    }
}

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

fun <T> QueryCommand.Context<T>.shouldPause(error: Throwable): QueryFetchStatus.Paused? {
    val pauseTime = options.pauseDurationAfter?.invoke(error)
    if (pauseTime != null && pauseTime.isPositive()) {
        return QueryFetchStatus.Paused(unpauseAt = pauseTime.toEpoch())
    }
    return null
}

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

suspend inline fun <T> QueryCommand.Context<T>.dispatchFetchResult(key: QueryKey<T>) {
    fetch(key)
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess(::dispatchFetchSuccess)
        .onFailure(::dispatchFetchFailure)
}

fun <T> QueryCommand.Context<T>.dispatchFetchSuccess(data: T) {
    val currentAt = epoch()
    val action = QueryAction.FetchSuccess(
        data = data,
        dataUpdatedAt = currentAt,
        dataStaleAt = options.staleTime.toEpoch(currentAt)
    )
    dispatch(action)
}

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
