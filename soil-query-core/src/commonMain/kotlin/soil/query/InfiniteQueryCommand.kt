// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.RetryFn
import soil.query.internal.exponentialBackOff
import kotlin.coroutines.cancellation.CancellationException

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

suspend inline fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.dispatchFetchChunksResult(
    key: InfiniteQueryKey<T, S>,
    variable: S
) {
    fetch(key, variable)
        .map { QueryChunk(it, variable) }
        .map { chunk -> state.data.orEmpty() + chunk }
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess(::dispatchFetchSuccess)
        .onFailure(::dispatchFetchFailure)
}

suspend inline fun <T, S> QueryCommand.Context<QueryChunks<T, S>>.dispatchRevalidateChunksResult(
    key: InfiniteQueryKey<T, S>,
    chunks: QueryChunks<T, S>
) {
    revalidate(key, chunks)
        .run { key.onRecoverData()?.let(::recoverCatching) ?: this }
        .onSuccess(::dispatchFetchSuccess)
        .onFailure(::dispatchFetchFailure)
}
