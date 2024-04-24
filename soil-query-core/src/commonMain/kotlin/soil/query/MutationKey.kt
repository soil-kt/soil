// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.SurrogateKey
import soil.query.internal.UniqueId
import soil.query.internal.uuid

interface MutationKey<T, S> {
    val id: MutationId<T, S>
    val mutate: suspend MutationReceiver.(variable: S) -> T
    val options: MutationOptions?

    // Pessimistic Updates
    // https://redux-toolkit.js.org/rtk-query/usage/manual-cache-updates#pessimistic-updates
    fun onQueryUpdate(variable: S, data: T): QueryEffect? = null
}

@Suppress("unused")
open class MutationId<T, S>(
    override val namespace: String,
    override vararg val tags: SurrogateKey
) : UniqueId {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutationId<*, *>) return false
        if (namespace != other.namespace) return false
        return tags.contentEquals(other.tags)
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + tags.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "MutationId(namespace='$namespace', tags=${tags.contentToString()})"
    }

    companion object {
        fun <T, S> auto(
            namespace: String = "auto/${uuid()}",
            vararg tags: SurrogateKey
        ): MutationId<T, S> {
            return MutationId(namespace, *tags)
        }
    }
}

fun <T, S> buildMutationKey(
    id: MutationId<T, S> = MutationId.auto(),
    mutate: suspend MutationReceiver.(variable: S) -> T,
    options: MutationOptions? = null
): MutationKey<T, S> {
    return object : MutationKey<T, S> {
        override val id: MutationId<T, S> = id
        override val mutate: suspend MutationReceiver.(S) -> T = mutate
        override val options: MutationOptions? = options
    }
}
