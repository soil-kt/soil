// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import java.util.UUID

actual fun epoch(): Long {
    return System.currentTimeMillis() / 1000
}

actual fun uuid(): String {
    return UUID.randomUUID().toString()
}
