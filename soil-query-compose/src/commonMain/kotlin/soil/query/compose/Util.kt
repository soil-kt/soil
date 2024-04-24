// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.launchIn
import soil.query.InfiniteQueryKey
import soil.query.MutationKey
import soil.query.QueryKey
import soil.query.ResumeQueriesFilter
import soil.query.SwrClient


typealias QueriesErrorReset = () -> Unit

@Composable
fun rememberQueriesErrorReset(
    filter: ResumeQueriesFilter = remember { ResumeQueriesFilter(predicate = { it.isFailure }) },
    client: SwrClient = LocalSwrClient.current
): QueriesErrorReset {
    val reset = remember(client) {
        { client.perform { resumeQueries(filter) } }
    }
    return reset
}

@Composable
fun KeepAlive(
    key: QueryKey<*>,
    client: SwrClient = LocalSwrClient.current
) {
    val query = remember(key) { client.getQuery(key) }
    LaunchedEffect(Unit) {
        query.actor.launchIn(this)
    }
}

@Composable
fun KeepAlive(
    key: InfiniteQueryKey<*, *>,
    client: SwrClient = LocalSwrClient.current
) {
    val query = remember(key) { client.getInfiniteQuery(key) }
    LaunchedEffect(Unit) {
        query.actor.launchIn(this)
    }
}

@Composable
fun KeepAlive(
    key: MutationKey<*, *>,
    client: SwrClient = LocalSwrClient.current
) {
    val query = remember(key) { client.getMutation(key) }
    LaunchedEffect(Unit) {
        query.actor.launchIn(this)
    }
}
