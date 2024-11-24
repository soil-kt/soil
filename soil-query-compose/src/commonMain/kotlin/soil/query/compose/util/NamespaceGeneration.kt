// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
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
    get() = Saver<Namespace, String>(save = { it.value }, restore = { Namespace(it) })


/**
 * Automatically generated value for `mutationId` and `subscriptionId`.
 *
 * This property is useful for generating a unique namespace when a key,
 * such as `MutationKey` or `SubscriptionKey`, is used within a single Composable function.
 *
 * **NOTE:**
 * This property must only be used within a Composable function restricted to a single key
 * (either Mutation or Subscription). It is intended to be used in a custom Composable function
 * dedicated to handling a single key. For other use cases, it is recommended to use the [auto] function.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun rememberCreatePost(): MutationObject<Post> {
 *     return rememberMutation(CreatePostKey(Namespace.autoCompositionKeyHash))
 * }
 * ```
 */
val Namespace.Companion.autoCompositionKeyHash: Namespace
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return Namespace("auto/$keyHash")
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
