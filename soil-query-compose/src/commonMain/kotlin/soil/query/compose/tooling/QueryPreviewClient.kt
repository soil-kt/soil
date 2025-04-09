// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.InfiniteQueryTestTag
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryReceiver
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.QueryTestTag
import soil.query.core.Marker
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.marker.TestTagMarker

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
    private val previewDataByTag: Map<TestTag, QueryState<*>>
) : QueryClient {

    override val queryReceiver: QueryReceiver = QueryReceiver

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker
    ): QueryRef<T> {
        val state = previewData[key.id] as? QueryState<T>
            ?: marker[TestTagMarker.Key]?.value?.let { previewDataByTag[it] as? QueryState<T> }
            ?: QueryState.initial()
        return SnapshotQuery(key.id, MutableStateFlow(state))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): InfiniteQueryRef<T, S> {
        val state = previewData[key.id] as? QueryState<QueryChunks<T, S>>
            ?: marker[TestTagMarker.Key]?.value?.let { previewDataByTag[it] as? QueryState<QueryChunks<T, S>> }
            ?: QueryState.initial()
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
        override fun close() = Unit
        override suspend fun resume() = Unit
        override suspend fun invalidate() = Unit
        override suspend fun join() = Unit
    }

    private class SnapshotInfiniteQuery<T, S>(
        private val key: InfiniteQueryKey<T, S>,
        override val state: StateFlow<QueryState<QueryChunks<T, S>>>
    ) : InfiniteQueryRef<T, S> {
        override val id: InfiniteQueryId<T, S> = key.id
        override fun close() = Unit
        override fun nextParam(data: QueryChunks<T, S>): S? = key.loadMoreParam(data)
        override suspend fun resume() = Unit
        override suspend fun loadMore(param: S) = Unit
        override suspend fun invalidate() = Unit
        override suspend fun join() = Unit
    }

    /**
     * Builder for [QueryPreviewClient].
     */
    class Builder {
        private val previewData = mutableMapOf<UniqueId, QueryState<*>>()
        private val previewDataByTag = mutableMapOf<TestTag, QueryState<*>>()

        /**
         * Registers a preview state for the query with the specified ID.
         *
         * @param id The query ID that identifies this query
         * @param snapshot A function that provides the query state to be returned for this ID
         */
        fun <T> on(id: QueryId<T>, snapshot: () -> QueryState<T>) {
            previewData[id] = snapshot()
        }

        /**
         * Registers a preview state for the query with the specified test tag.
         *
         * @param testTag The test tag that identifies this query
         * @param snapshot A function that provides the query state to be returned for this tag
         */
        fun <T> on(testTag: QueryTestTag<T>, snapshot: () -> QueryState<T>) {
            previewDataByTag[testTag] = snapshot()
        }

        /**
         * Registers a preview state for the infinite query with the specified ID.
         *
         * @param id The infinite query ID that identifies this infinite query
         * @param snapshot A function that provides the query state to be returned for this ID
         */
        fun <T, S> on(id: InfiniteQueryId<T, S>, snapshot: () -> QueryState<QueryChunks<T, S>>) {
            previewData[id] = snapshot()
        }

        /**
         * Registers a preview state for the infinite query with the specified test tag.
         *
         * @param testTag The test tag that identifies this infinite query
         * @param snapshot A function that provides the query state to be returned for this tag
         */
        fun <T, S> on(testTag: InfiniteQueryTestTag<T, S>, snapshot: () -> QueryState<QueryChunks<T, S>>) {
            previewDataByTag[testTag] = snapshot()
        }

        /**
         * Builds a new instance of [QueryPreviewClient] with the registered preview states.
         *
         * @return A new [QueryPreviewClient] instance
         */
        fun build() = QueryPreviewClient(previewData, previewDataByTag)
    }
}

/**
 * Create a [QueryPreviewClient] instance with the provided [initializer].
 *
 * @param initializer A lambda with [QueryPreviewClient.Builder] receiver that initializes preview states
 * @return A query client that can be used to provide mock data for queries in Compose previews
 */
fun QueryPreviewClient(initializer: QueryPreviewClient.Builder.() -> Unit): QueryPreviewClient {
    return QueryPreviewClient.Builder().apply(initializer).build()
}
