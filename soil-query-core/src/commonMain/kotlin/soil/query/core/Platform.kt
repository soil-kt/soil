// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlin.time.Duration

/**
 * Returns the current epoch time.
 *
 * @return Epoch seconds
 */
expect fun epoch(): Long

/**
 * Generate a Version 4 UUID.
 *
 * @return UUID string.
 */
expect fun uuid(): String

internal fun Duration.toEpoch(at: Long = epoch()): Long {
    val leftover = Long.MAX_VALUE - at
    return at + inWholeSeconds.coerceAtMost(leftover)
}
