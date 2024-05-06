// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

/**
 * Interface providing settings for logging output for debugging purposes.
 */
interface LoggingOptions {

    /**
     * Specifies the logger function.
     *
     * **Note:** When LoggerFn is set, it writes internal logic state changes to the log.
     */
    val logger: LoggerFn?
}

internal inline fun LoggingOptions.vvv(id: UniqueId, message: () -> String) {
    logger?.log("$id\n${message()}")
}
