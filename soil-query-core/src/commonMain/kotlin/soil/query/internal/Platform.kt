// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlin.time.Duration

expect fun epoch(): Long

expect fun uuid(): String

internal fun Duration.toEpoch(at: Long = epoch()): Long {
    return at + inWholeSeconds
}
