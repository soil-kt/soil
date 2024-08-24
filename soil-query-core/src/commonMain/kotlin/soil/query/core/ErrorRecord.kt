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
     * Key information that caused the error.
     *
     * NOTE: Defining an ID with a custom interface, such as metadata, can be helpful when receiving error information.
     */
    val key: UniqueId
)
