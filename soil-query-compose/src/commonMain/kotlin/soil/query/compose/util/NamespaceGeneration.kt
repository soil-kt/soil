// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import soil.query.core.Namespace
import soil.query.core.uuid

/**
 * Automatically generated value for mutationId and subscriptionId.
 *
 * This function is useful for generating a unique namespace when a key, such as MutationKey or SubscriptionKey, is used in a single Composable function.
 *
 * @see Namespace
 */
@Composable
fun Namespace.Companion.auto(): Namespace {
    return rememberSaveable(saver = Namespace.Saver) { Namespace("auto/${uuid()}") }
}

internal val Namespace.Companion.Saver
    get() = Saver<Namespace, String>(
        save = { it.value },
        restore = { Namespace(it) }
    )
