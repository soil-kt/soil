// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.vvv

sealed class QueryCommands<T> : QueryCommand<T> {
    data class Connect<T>(
        val key: QueryKey<T>,
        val revision: String? = null
    ) : QueryCommands<T>() {

        override suspend fun handle(ctx: QueryCommand.Context<T>) {
            if (!ctx.shouldFetch(revision)) {
                ctx.options.vvv(key.id) { "skip fetch(shouldFetch=false)" }
                return
            }
            ctx.dispatch(QueryAction.Fetching())
            ctx.dispatchFetchResult(key)
        }
    }

    data class Invalidate<T>(
        val key: QueryKey<T>,
        val revision: String
    ) : QueryCommands<T>() {

        override suspend fun handle(ctx: QueryCommand.Context<T>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip fetch(revision is not matched)" }
                return
            }
            ctx.dispatch(QueryAction.Fetching(isInvalidated = true))
            ctx.dispatchFetchResult(key)
        }
    }
}
