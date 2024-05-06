// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * Extension receiver for referencing external instances needed when executing query.
 *
 * Usage:
 *
 * ```kotlin
 * class KtorReceiver(
 *     val client: HttpClient
 * ) : QueryReceiver, MutationReceiver
 * ```
 */
interface QueryReceiver {

    /**
     * Default implementation for [QueryReceiver].
     */
    companion object : QueryReceiver
}
