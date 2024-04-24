// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.vvv

sealed class InfiniteQueryCommands<T, S> : QueryCommand<QueryChunks<T, S>> {

    data class Connect<T, S>(
        val key: InfiniteQueryKey<T, S>,
        val revision: String? = null
    ) : InfiniteQueryCommands<T, S>() {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            if (!ctx.shouldFetch(revision)) {
                ctx.options.vvv(key.id) { "skip fetch(shouldFetch=false)" }
                return
            }
            ctx.dispatch(QueryAction.Fetching())
            val chunks = ctx.state.data
            if (chunks.isNullOrEmpty() || ctx.state.isPlaceholderData) {
                ctx.dispatchFetchChunksResult(key, key.initialParam())
            } else {
                ctx.dispatchRevalidateChunksResult(key, chunks)
            }
        }
    }

    data class Invalidate<T, S>(
        val key: InfiniteQueryKey<T, S>,
        val revision: String
    ) : InfiniteQueryCommands<T, S>() {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip fetch(revision is not matched)" }
                return
            }
            ctx.dispatch(QueryAction.Fetching(isInvalidated = true))
            val chunks = ctx.state.data
            if (chunks.isNullOrEmpty() || ctx.state.isPlaceholderData) {
                ctx.dispatchFetchChunksResult(key, key.initialParam())
            } else {
                ctx.dispatchRevalidateChunksResult(key, chunks)
            }
        }
    }

    data class LoadMore<T, S>(
        val key: InfiniteQueryKey<T, S>,
        val param: S
    ) : InfiniteQueryCommands<T, S>() {
        override suspend fun handle(ctx: QueryCommand.Context<QueryChunks<T, S>>) {
            val chunks = ctx.state.data
            if (param != key.loadMoreParam(chunks.orEmpty())) {
                ctx.options.vvv(key.id) { "skip fetch(param is changed)" }
                return
            }

            ctx.dispatch(QueryAction.Fetching())
            ctx.dispatchFetchChunksResult(key, param)
        }
    }
}
