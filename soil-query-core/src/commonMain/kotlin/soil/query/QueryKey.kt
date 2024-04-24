// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.SurrogateKey
import soil.query.internal.UniqueId

interface QueryKey<T> {
    val id: QueryId<T>
    val fetch: suspend QueryReceiver.() -> T
    val options: QueryOptions?

    fun onPlaceholderData(): QueryPlaceholderData<T>? = null

    fun onRecoverData(): QueryRecoverData<T>? = null
}

@Suppress("unused")
open class QueryId<T>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueryId<*>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "QueryId(namespace='$namespace', tags=${tags.contentToString()})"
    }
}

fun <T> buildQueryKey(
    id: QueryId<T>,
    fetch: suspend QueryReceiver.() -> T,
    options: QueryOptions? = null
): QueryKey<T> {
    return object : QueryKey<T> {
        override val id: QueryId<T> = id
        override val fetch: suspend QueryReceiver.() -> T = fetch
        override val options: QueryOptions? = options
    }
}
