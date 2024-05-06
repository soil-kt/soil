// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * Extension receiver for referencing external instances needed when executing [mutate][MutationKey.mutate].
 *
 * Usage:
 *
 * ```kotlin
 * class KtorReceiver(
 *     val client: HttpClient
 * ) : QueryReceiver, MutationReceiver
 * ```
 */
interface MutationReceiver {

    /**
     * Default implementation for [MutationReceiver].
     */
    companion object : MutationReceiver
}
