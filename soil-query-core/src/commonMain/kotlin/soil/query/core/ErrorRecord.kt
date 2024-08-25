// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Error information that can be received via a back-channel
 * such as [ErrorRelay] or `onError` options for Query/Mutation.
 */
data class ErrorRecord internal constructor(

    /**
     * The details of the error.
     */
    val exception: Throwable,

    /**
     * The unique identifier of the key that caused the error.
     */
    val keyId: UniqueId,

    /**
     * The marker that was set when the error occurred.
     */
    val marker: Marker
)
