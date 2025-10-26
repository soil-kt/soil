// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlin.js.Date

actual fun epoch(): Long {
    return Date.now().toString().toLong() / 1000
}
