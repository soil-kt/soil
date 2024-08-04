// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Logger functional interface to output log messages.
 *
 * This functional interface provides logging for debugging purposes for developers.
 */
fun interface LoggerFn {

    /**
     * Outputs the log message.
     *
     * @param message Log message.
     */
    fun log(message: String)
}
