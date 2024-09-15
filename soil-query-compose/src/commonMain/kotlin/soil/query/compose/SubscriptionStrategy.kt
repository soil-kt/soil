// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import soil.query.SubscriptionRef
import soil.query.SubscriptionState

/**
 * A mechanism to finely adjust the behavior of the subscription on a component basis in Composable functions.
 *
 * If you want to customize, please create a class implementing [SubscriptionStrategy].
 * For example, this is useful when you want to switch your implementation to `collectAsStateWithLifecycle`.
 */
@Stable
interface SubscriptionStrategy {

    @Composable
    fun <T> collectAsState(subscription: SubscriptionRef<T>): SubscriptionState<T>

    companion object
}

/**
 * The default built-in strategy for Subscription built into the library.
 */
val SubscriptionStrategy.Companion.Default: SubscriptionStrategy
    get() = DefaultSubscriptionStrategy

private object DefaultSubscriptionStrategy : SubscriptionStrategy {
    @Composable
    override fun <T> collectAsState(subscription: SubscriptionRef<T>): SubscriptionState<T> {
        val state by subscription.state.collectAsState()
        LaunchedEffect(subscription.id) {
            subscription.resume()
        }
        return state
    }
}

/**
 * Strategy for manually starting the Subscription without automatic subscription.
 */
val SubscriptionStrategy.Companion.Lazy: SubscriptionStrategy
    get() = LazySubscriptionStrategy

private object LazySubscriptionStrategy : SubscriptionStrategy {
    @Composable
    override fun <T> collectAsState(subscription: SubscriptionRef<T>): SubscriptionState<T> {
        return subscription.state.collectAsState().value
    }
}

// FIXME: CompositionLocal LocalLifecycleOwner not present
//  Android, it works only with Compose UI 1.7.0-alpha05 or above.
//  Therefore, we will postpone adding this code until a future release.
//val SubscriptionStrategy.Companion.Lifecycle: SubscriptionStrategy
//    get() = LifecycleSubscriptionStrategy
//
//private object LifecycleSubscriptionStrategy : SubscriptionStrategy {
//    @Composable
//    override fun <T> collectAsState(subscription: SubscriptionRef<T>): SubscriptionState<T> {
//        val state by subscription.state.collectAsStateWithLifecycle()
//        LifecycleStartEffect(subscription.id) {
//            lifecycleScope.launch { subscription.resume() }
//            onStopOrDispose { subscription.cancel() }
//        }
//        return state
//    }
//}
