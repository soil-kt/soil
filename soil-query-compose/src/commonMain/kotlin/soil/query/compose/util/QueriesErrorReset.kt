// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.query.ResumeQueriesFilter
import soil.query.SwrClient
import soil.query.compose.LocalSwrClient


typealias QueriesErrorReset = () -> Unit

/**
 * Remember a [QueriesErrorReset] to resume all queries with [filter] matched.
 *
 * @param filter The filter to match queries.
 * @param client The [SwrClient] to resume queries. By default, it uses the [LocalSwrClient].
 * @return A [QueriesErrorReset] to resume queries.
 */
@Composable
fun rememberQueriesErrorReset(
    filter: ResumeQueriesFilter = remember { ResumeQueriesFilter(predicate = { it.isFailure }) },
    client: SwrClient = LocalSwrClient.current
): QueriesErrorReset {
    val reset = remember<() -> Unit>(client) {
        { client.perform { resumeQueries(filter) } }
    }
    return reset
}
