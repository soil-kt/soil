// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import platform.Foundation.NSDate
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970

actual fun epoch(): Long {
    return (NSDate().timeIntervalSince1970).toLong()
}

actual fun uuid(): String {
    return NSUUID().UUIDString()
}
