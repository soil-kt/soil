// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.InfiniteQueryCommand
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryCommand
import soil.query.QueryKey
import soil.query.QueryOptions
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.core.UniqueId

/**
 * Usage:
 * ```kotlin
 * val client = QueryPreviewClient(
 *   previewData = mapOf(
 *      MyQueryId to QueryState.success("data"),
 *      ..
 *   )
 * )
 * ```
 */
@Stable
class QueryPreviewClient(
    private val previewData: Map<UniqueId, QueryState<*>>,
    override val defaultQueryOptions: QueryOptions = QueryOptions
) : QueryClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(key: QueryKey<T>): QueryRef<T> {
        val state = previewData[key.id] as? QueryState<T> ?: QueryState.initial()
        val options = key.onConfigureOptions()?.invoke(defaultQueryOptions) ?: defaultQueryOptions
        return SnapshotQuery(key, options, MutableStateFlow(state))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(key: InfiniteQueryKey<T, S>): InfiniteQueryRef<T, S> {
        val state = previewData[key.id] as? QueryState<QueryChunks<T, S>> ?: QueryState.initial()
        val options = key.onConfigureOptions()?.invoke(defaultQueryOptions) ?: defaultQueryOptions
        return SnapshotInfiniteQuery(key, options, MutableStateFlow(state))
    }

    override fun <T> prefetchQuery(key: QueryKey<T>): Job = Job()

    override fun <T, S> prefetchInfiniteQuery(key: InfiniteQueryKey<T, S>): Job = Job()

    private class SnapshotQuery<T>(
        override val key: QueryKey<T>,
        override val options: QueryOptions,
        override val state: StateFlow<QueryState<T>>
    ) : QueryRef<T> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun send(command: QueryCommand<T>) = Unit
        override suspend fun resume() = Unit
        override suspend fun invalidate() = Unit
    }

    private class SnapshotInfiniteQuery<T, S>(
        override val key: InfiniteQueryKey<T, S>,
        override val options: QueryOptions,
        override val state: StateFlow<QueryState<QueryChunks<T, S>>>
    ) : InfiniteQueryRef<T, S> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun send(command: InfiniteQueryCommand<T, S>) = Unit
        override suspend fun resume() = Unit
        override suspend fun loadMore(param: S) = Unit
        override suspend fun invalidate() = Unit
    }
}
