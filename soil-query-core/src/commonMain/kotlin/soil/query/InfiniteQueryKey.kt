// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.SurrogateKey
import soil.query.internal.UniqueId

interface InfiniteQueryKey<T, S> {
    val id: InfiniteQueryId<T, S>
    val fetch: suspend QueryReceiver.(param: S) -> T
    val initialParam: () -> S
    val loadMoreParam: (chunks: QueryChunks<T, S>) -> S?
    val options: QueryOptions?

    fun onPlaceholderData(): QueryPlaceholderData<QueryChunks<T, S>>? = null

    fun onRecoverData(): QueryRecoverData<QueryChunks<T, S>>? = null
}

@Suppress("unused")
open class InfiniteQueryId<T, S>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InfiniteQueryId<*, *>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "InfiniteQueryId(namespace='$namespace', tags=${tags.contentToString()})"
    }
}

internal fun <T, S> InfiniteQueryKey<T, S>.hasMore(chunks: QueryChunks<T, S>): Boolean {
    return loadMoreParam(chunks) != null
}

fun <T, S> buildInfiniteQueryKey(
    id: InfiniteQueryId<T, S>,
    fetch: suspend QueryReceiver.(param: S) -> T,
    initialParam: () -> S,
    loadMoreParam: (QueryChunks<T, S>) -> S?,
    options: QueryOptions? = null
): InfiniteQueryKey<T, S> {
    return object : InfiniteQueryKey<T, S> {
        override val id: InfiniteQueryId<T, S> = id
        override val fetch: suspend QueryReceiver.(param: S) -> T = fetch
        override val initialParam: () -> S = initialParam
        override val loadMoreParam: (QueryChunks<T, S>) -> S? = loadMoreParam
        override val options: QueryOptions? = options
    }
}
