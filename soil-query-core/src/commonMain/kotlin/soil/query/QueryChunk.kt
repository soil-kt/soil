// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * Data chunk type that holds values in combination with [param] related to [data].
 *
 * In [InfiniteQueryKey], multiple data fetches are performed due to additional retrieval.
 * [QueryChunk] is a data structure for holding the result of one fetch. [QueryChunks] holds multiple [QueryChunk] results.
 */
data class QueryChunk<T, S>(
    val data: T,
    val param: S
)

typealias QueryChunks<T, S> = List<QueryChunk<T, S>>

/**
 * Returns the data of all chunks.
 */
val <T, S> QueryChunks<List<T>, S>.chunkedData: List<T>
    get() = flatMap { it.data }

/**
 * Transforms all chunk data with [transform] and returns the extracted data using [selector].
 */
inline fun <T, S, U, E> QueryChunks<T, S>.chunked(
    transform: (QueryChunk<T, S>) -> Iterable<U>,
    selector: (U) -> E
): List<E> {
    return flatMap(transform).map(selector)
}

/**
 * Modifies the data of all chunks that match the condition.
 */
fun <T, S> QueryChunks<List<T>, S>.modifyData(
    match: (T) -> Boolean,
    edit: T.() -> T
): QueryChunks<List<T>, S> {
    return map { chunk ->
        val modified = chunk.data.map { entity ->
            if (match(entity)) entity.edit()
            else entity
        }
        chunk.copy(data = modified)
    }
}
