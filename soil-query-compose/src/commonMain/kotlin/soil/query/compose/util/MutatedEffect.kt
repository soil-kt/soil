// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import soil.query.compose.MutationObject
import soil.query.compose.MutationSuccessObject

/**
 * A Composable function to trigger side effects when a Mutation is successfully processed.
 *
 * By using this function, you can receive the result of a successful Mutation via [block].
 * This is particularly useful when working with [MutationObject.mutateAsync].
 *
 * @param T Type of the return value from the mutation.
 * @param U Type of the key to identify whether the Mutation has already been handled.
 * @param mutation The MutationObject whose result will be observed.
 * @param keySelector A function to calculate a key to identify whether the Mutation has already been handled.
 *                    The key is compared upon the next success.
 * @param keySaver A Saver to persist and restore the last consumed key.
 * @param block A callback to handle the result of the Mutation. This is called only when the key differs from the previous one.
 */
@Composable
fun <T, U : Any> MutatedEffect(
    mutation: MutationObject<T, *>,
    keySelector: (MutationSuccessObject<T, *>) -> U,
    keySaver: Saver<U?, out Any> = autoSaver(),
    block: suspend (data: T) -> Unit
) {
    val mutationState by rememberUpdatedState(mutation)
    var lastConsumedKey by rememberSaveable(stateSaver = keySaver) { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { mutationState as? MutationSuccessObject }
            .filterNotNull()
            .collect {
                val mutatedKey = keySelector(it)
                if (lastConsumedKey != mutatedKey) {
                    lastConsumedKey = mutatedKey
                    block(it.data)
                }
            }
    }
}

/**
 * A Composable function to trigger side effects when a Mutation is successfully processed.
 *
 * This function uses [MutationObject.mutatedCount] as the key.
 * If you need to use a different key, use [MutatedEffect] with the `keySelector` parameter.
 *
 * **NOTE:**
 * If Mutation optimization is enabled, you must explicitly specify a `keySelector`.
 * This is because [MutationObject.mutatedCount] is omitted during optimization and will always be `0`.
 *
 * @param T Type of the return value from the mutation.
 * @param mutation The MutationObject whose result will be observed.
 */
@Composable
inline fun <T> MutatedEffect(
    mutation: MutationObject<T, *>,
    noinline block: suspend (data: T) -> Unit
) {
    MutatedEffect(
        mutation = mutation,
        keySelector = { it.mutatedCount },
        block = block
    )
}
