// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlin.time.Duration

interface ActorOptions {
    val keepAliveTime: Duration
}

fun ActorOptions.newSharingStarted(
    onActive: ActorCallback? = null,
    onInactive: ActorCallback? = null
): ActorSharingStarted {
    return ActorSharingStarted(
        keepAliveTime = keepAliveTime,
        onActive = onActive,
        onInactive = onInactive
    )
}
