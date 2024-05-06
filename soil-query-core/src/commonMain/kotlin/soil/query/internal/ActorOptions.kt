// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlin.time.Duration

/**
 * Interface providing settings related to the internal behavior of an Actor.
 *
 * The actual Actor is managed as a Coroutine Flow for each [UniqueId].
 * Using [newSharingStarted], it remains active only when there are subscribers.
 */
interface ActorOptions {

    /**
     * Duration to remain state as an active.
     *
     * The temporary measure to keep the state active once it's no longer referenced anywhere.
     *
     * **Note:** On the Android platform, there is a possibility of temporarily unsubscribing due to configuration changes such as screen rotation.
     * Set a duration of at least a few seconds to ensure that the Job processing within the Coroutine Flow is not canceled.
     * This value has the same meaning as [SharingStarted.WhileSubscribed(5_000)](https://github.com/search?q=repo%3Aandroid%2Fnowinandroid+WhileSubscribed) defined in ViewModel in [Now in Android App](https://github.com/android/nowinandroid).
     */
    val keepAliveTime: Duration
}

/**
 * Creates a new [ActorSharingStarted] specific for Actor.
 *
 * @param onActive Callback handler to notify when becoming active.
 * @param onInactive Callback handler to notify when becoming inactive.
 */
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
