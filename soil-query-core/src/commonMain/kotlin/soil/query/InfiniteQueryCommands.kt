// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Marker
import soil.query.core.getOrElse
import soil.query.core.getOrNull
import soil.query.core.vvv
import kotlin.coroutines.cancellation.CancellationException

object InfiniteQueryCommands {

    /**
     * Performs data fetching and validation based on the current data state.
     *
     * This command is invoked via [InfiniteQueryRef] when a new mount point (subscriber) is added.
     *
     * @param key Instance of a class implementing [InfiniteQueryKey].
     * @param revision The revision of the data to be fetched.
     * @param marker The marker with additional information based on the caller of a query.
     * @param callback The callback to receive the result of the query.
     */
    class Connect<T, S>(
        private val key: InfiniteQueryKey<T, S>,
        private val revision: String? = null,
        private val marker: Marker = Marker.None,
        private val callback: QueryCallback<QueryChunks<T, S>>? = null
    ) : InfiniteQueryCommand<T, S> {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            if (!ctx.shouldFetch(revision)) {
                ctx.options.vvv(key.id) { "skip fetch(shouldFetch=false)" }
                callback?.invoke(Result.failure(CancellationException("skip fetch")))
                return
            }
            ctx.dispatch(QueryAction.Fetching())
            val chunks = ctx.state.reply.getOrNull()
            if (chunks.isNullOrEmpty()) {
                ctx.dispatchFetchChunksResult(key, key.initialParam(), marker, callback)
            } else {
                ctx.dispatchRevalidateChunksResult(key, chunks, marker, callback)
            }
        }
    }

    /**
     * Invalidates the data and performs data fetching and validation based on the current data state.
     *
     * This command is invoked via [InfiniteQueryRef] when the data is invalidated.
     *
     * @param key Instance of a class implementing [InfiniteQueryKey].
     * @param revision The revision of the data to be invalidated.
     * @param marker The marker with additional information based on the caller of a query.
     * @param callback The callback to receive the result of the query.
     */
    class Invalidate<T, S>(
        private val key: InfiniteQueryKey<T, S>,
        private val revision: String,
        private val marker: Marker = Marker.None,
        private val callback: QueryCallback<QueryChunks<T, S>>? = null
    ) : InfiniteQueryCommand<T, S> {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip fetch(revision is not matched)" }
                callback?.invoke(Result.failure(CancellationException("skip fetch")))
                return
            }
            ctx.dispatch(QueryAction.Fetching(isInvalidated = true))
            val chunks = ctx.state.reply.getOrNull()
            if (chunks.isNullOrEmpty()) {
                ctx.dispatchFetchChunksResult(key, key.initialParam(), marker, callback)
            } else {
                ctx.dispatchRevalidateChunksResult(key, chunks, marker, callback)
            }
        }
    }

    /**
     * Fetches additional data for [InfiniteQueryKey] using [param].
     *
     * @param key Instance of a class implementing [InfiniteQueryKey].
     * @param param The parameter required for fetching data for [InfiniteQueryKey].
     * @param marker The marker with additional information based on the caller of a query.
     * @param callback The callback to receive the result of the query.
     */
    class LoadMore<T, S>(
        private val key: InfiniteQueryKey<T, S>,
        private val param: S,
        private val marker: Marker = Marker.None,
        private val callback: QueryCallback<QueryChunks<T, S>>? = null
    ) : InfiniteQueryCommand<T, S> {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            val chunks = ctx.state.reply.getOrElse { emptyList() }
            if (param != key.loadMoreParam(chunks)) {
                ctx.options.vvv(key.id) { "skip fetch(param is changed)" }
                callback?.invoke(Result.failure(CancellationException("skip fetch")))
                return
            }

            ctx.dispatch(QueryAction.Fetching())
            ctx.dispatchFetchChunksResult(key, param, marker, callback)
        }
    }
}
