// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryOptions
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.core.Marker
import soil.query.core.UniqueId

/**
 * Usage:
 * ```kotlin
 * val queryClient = QueryPreviewClient {
 *     on(MyQueryId1) { QueryState.success("data") }
 *     on(MyQueryId2) { .. }
 * }
 * ```
 */
@Stable
class QueryPreviewClient(
    private val previewData: Map<UniqueId, QueryState<*>>,
    override val defaultQueryOptions: QueryOptions = QueryOptions
) : QueryClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker
    ): QueryRef<T> {
        val state = previewData[key.id] as? QueryState<T> ?: QueryState.initial()
        return SnapshotQuery(key.id, MutableStateFlow(state))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): InfiniteQueryRef<T, S> {
        val state = previewData[key.id] as? QueryState<QueryChunks<T, S>> ?: QueryState.initial()
        return SnapshotInfiniteQuery(key, MutableStateFlow(state))
    }

    override fun <T> prefetchQuery(
        key: QueryKey<T>,
        marker: Marker
    ): Job = Job()

    override fun <T, S> prefetchInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): Job = Job()

    private class SnapshotQuery<T>(
        override val id: QueryId<T>,
        override val state: StateFlow<QueryState<T>>
    ) : QueryRef<T> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun resume() = Unit
        override suspend fun invalidate() = Unit
    }

    private class SnapshotInfiniteQuery<T, S>(
        private val key: InfiniteQueryKey<T, S>,
        override val state: StateFlow<QueryState<QueryChunks<T, S>>>
    ) : InfiniteQueryRef<T, S> {
        override val id: InfiniteQueryId<T, S> = key.id
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override fun nextParam(data: QueryChunks<T, S>): S? = key.loadMoreParam(data)
        override suspend fun resume() = Unit
        override suspend fun loadMore(param: S) = Unit
        override suspend fun invalidate() = Unit
    }

    /**
     * Builder for [QueryPreviewClient].
     */
    class Builder {
        private val previewData = mutableMapOf<UniqueId, QueryState<*>>()

        fun <T> on(id: QueryId<T>, snapshot: () -> QueryState<T>) {
            previewData[id] = snapshot()
        }

        fun <T, S> on(id: InfiniteQueryId<T, S>, snapshot: () -> QueryState<QueryChunks<T, S>>) {
            previewData[id] = snapshot()
        }

        fun build() = QueryPreviewClient(previewData)
    }
}

/**
 * Create a [QueryPreviewClient] instance with the provided [initializer].
 */
fun QueryPreviewClient(initializer: QueryPreviewClient.Builder.() -> Unit): QueryPreviewClient {
    return QueryPreviewClient.Builder().apply(initializer).build()
}
