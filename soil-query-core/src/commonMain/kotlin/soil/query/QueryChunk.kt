// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

data class QueryChunk<T, S>(
    val data: T,
    val param: S
)

typealias QueryChunks<T, S> = List<QueryChunk<T, S>>

val <T, S> QueryChunks<List<T>, S>.chunkedData: List<T>
    get() = flatMap { it.data }

inline fun <T, S, U, E> QueryChunks<T, S>.chunked(
    transform: (QueryChunk<T, S>) -> Iterable<U>,
    selector: (U) -> E
): List<E> {
    return flatMap(transform).map(selector)
}

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
