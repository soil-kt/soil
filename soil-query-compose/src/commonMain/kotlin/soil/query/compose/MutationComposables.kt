// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.query.MutationClient
import soil.query.MutationKey

/**
 * Provides a conditional [rememberMutation].
 *
 * Calls [rememberMutation] only if [keyFactory] returns a [MutationKey] from [value].
 *
 * @see rememberMutation
 */
@Composable
fun <T, S, V> rememberMutationIf(
    value: V,
    keyFactory: (value: V) -> MutationKey<T, S>?,
    config: MutationConfig = MutationConfig.Default,
    client: MutationClient = LocalMutationClient.current
): MutationObject<T, S>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberMutation(key, config, client)
}
