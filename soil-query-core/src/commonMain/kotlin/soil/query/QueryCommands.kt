// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.vvv
import kotlin.coroutines.cancellation.CancellationException

/**
 * Query command for [QueryKey].
 *
 * @param T Type of data to retrieve.
 */
sealed class QueryCommands<T> : QueryCommand<T> {

    /**
     * Performs data fetching and validation based on the current data state.
     *
     * This command is invoked via [QueryRef] when a new mount point (subscriber) is added.
     *
     * @param key Instance of a class implementing [QueryKey].
     * @param revision The revision of the data to be fetched.
     */
    data class Connect<T>(
        val key: QueryKey<T>,
        val revision: String? = null,
        val callback: QueryCallback<T>? = null
    ) : QueryCommands<T>() {

        override suspend fun handle(ctx: QueryCommand.Context<T>) {
            if (!ctx.shouldFetch(revision)) {
                ctx.options.vvv(key.id) { "skip fetch(shouldFetch=false)" }
                callback?.invoke(Result.failure(CancellationException("skip fetch")))
                return
            }
            ctx.dispatch(QueryAction.Fetching())
            ctx.dispatchFetchResult(key, callback)
        }
    }

    /**
     * Invalidates the data and performs data fetching and validation based on the current data state.
     *
     * This command is invoked via [QueryRef] when the data is invalidated.
     *
     * @param key Instance of a class implementing [QueryKey].
     * @param revision The revision of the data to be invalidated.
     */
    data class Invalidate<T>(
        val key: QueryKey<T>,
        val revision: String,
        val callback: QueryCallback<T>? = null
    ) : QueryCommands<T>() {

        override suspend fun handle(ctx: QueryCommand.Context<T>) {
            if (ctx.state.revision != revision) {
                ctx.options.vvv(key.id) { "skip fetch(revision is not matched)" }
                callback?.invoke(Result.failure(CancellationException("skip fetch")))
                return
            }
            ctx.dispatch(QueryAction.Fetching(isInvalidated = true))
            ctx.dispatchFetchResult(key, callback)
        }
    }
}
