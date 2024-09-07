// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.MutationClient
import soil.query.MutationKey

/**
 * Remember a [MutationObject] and subscribes to the mutation state of [key].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 * @param key The [MutationKey] for managing [mutation][soil.query.Mutation] associated with [id][soil.query.MutationId].
 * @param config The configuration for the mutation. By default, it uses the [MutationConfig.Default].
 * @param client The [MutationClient] to resolve [key]. By default, it uses the [LocalMutationClient].
 * @return A [MutationObject] each the mutation state changed.
 */
@Composable
fun <T, S> rememberMutation(
    key: MutationKey<T, S>,
    config: MutationConfig = MutationConfig.Default,
    client: MutationClient = LocalMutationClient.current
): MutationObject<T, S> {
    val scope = rememberCoroutineScope()
    val mutation = remember(key) { client.getMutation(key, config.marker).also { it.launchIn(scope) } }
    return with(config.mapper) {
        config.strategy.collectAsState(mutation).toObject(mutation = mutation)
    }
}
