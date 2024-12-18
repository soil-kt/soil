// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Marker
import soil.query.core.RetryFn
import soil.query.core.exponentialBackOff
import soil.query.core.getOrElse
import kotlin.coroutines.cancellation.CancellationException

/**
 * Query command for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
interface InfiniteQueryCommand<T, S> : QueryCommand<QueryChunks<T, S>>

/**
 * Fetches data for the [InfiniteQueryKey] using the value of [variable].
 *
 * @receiver [QueryCommand.Context] for [InfiniteQueryKey].
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key Instance of a class implementing [InfiniteQueryKey].
 * @param variable Value of the parameter required for fetching data for [InfiniteQueryKey].
 * @param retryFn Retry strategy.
 */
suspend fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.fetch(
    key: InfiniteQueryKey<T, S>,
    variable: S,
    retryFn: RetryFn<T> = options.exponentialBackOff(onRetry = onRetryCallback(key.id))
): Result<T> {
    return try {
        val value = retryFn.withRetry { with(key) { receiver.fetch(variable) } }
        Result.success(value)
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

/**
 * Revalidates the data for the [InfiniteQueryKey] using the value of [chunks].
 *
 * @receiver [QueryCommand.Context] for [InfiniteQueryKey].
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key Instance of a class implementing [InfiniteQueryKey].
 * @param chunks Data to revalidate.
 */
suspend fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.revalidate(
    key: InfiniteQueryKey<T, S>,
    chunks: QueryChunks<T, S>
): Result<QueryChunks<T, S>> {
    val newData = chunks.indices.fold(emptyList<QueryChunk<T, S>>()) { acc, i ->
        val param = if (i > 0) {
            checkNotNull(key.loadMoreParam(acc))
        } else {
            key.initialParam()
        }
        val result = fetch(key, param)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }
        val chunk = result.map { QueryChunk(it, param) }.getOrThrow()
        val next = acc + chunk
        if (!key.hasMore(next)) {
            return Result.success(next)
        }

        next
    }
    return Result.success(newData)
}

/**
 * Dispatches the result of fetching data for the [InfiniteQueryKey].
 *
 * @receiver [QueryCommand.Context] for [InfiniteQueryKey].
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key Instance of a class implementing [InfiniteQueryKey].
 * @param variable Value of the parameter required for fetching data for [InfiniteQueryKey].
 * @param marker The marker with additional information based on the caller of a query.
 * @param callback Callback to handle the result.
 */
suspend inline fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.dispatchFetchChunksResult(
    key: InfiniteQueryKey<T, S>,
    variable: S,
    marker: Marker,
    noinline callback: QueryCallback<QueryChunks<T, S>>?
) {
    fetch(key, variable)
        .map { QueryChunk(it, variable) }
        .map { chunk -> state.reply.getOrElse { emptyList() } + chunk }
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess { dispatchFetchSuccess(it, key.contentEquals) }
        .onFailure(::dispatchFetchFailure)
        .onFailure { reportQueryError(it, key.id, marker) }
        .also { callback?.invoke(it) }
}

/**
 * Dispatches the result of revalidating data for the [InfiniteQueryKey].
 *
 * @receiver [QueryCommand.Context] for [InfiniteQueryKey].
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key Instance of a class implementing [InfiniteQueryKey].
 * @param chunks Data to revalidate.
 * @param marker The marker with additional information based on the caller of a query.
 * @param callback Callback to handle the result.
 */
suspend inline fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.dispatchRevalidateChunksResult(
    key: InfiniteQueryKey<T, S>,
    chunks: QueryChunks<T, S>,
    marker: Marker,
    noinline callback: QueryCallback<QueryChunks<T, S>>?
) {
    revalidate(key, chunks)
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess { dispatchFetchSuccess(it, key.contentEquals) }
        .onFailure(::dispatchFetchFailure)
        .onFailure { reportQueryError(it, key.id, marker) }
        .also { callback?.invoke(it) }
}
