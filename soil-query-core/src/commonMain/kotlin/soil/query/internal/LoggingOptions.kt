// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

interface LoggingOptions {
    val logger: LoggerFn?
}

internal inline fun LoggingOptions.vvv(id: UniqueId, message: () -> String) {
    logger?.log("$id\n${message()}")
}
