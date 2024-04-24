// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import soil.query.SwrClient
import soil.query.internal.uuid

@Composable
fun SwrClientProvider(
    client: SwrClient,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSwrClient provides client) {
        content()
    }
    DisposableEffect(client) {
        val id = uuid()
        client.onMount(id)
        onDispose {
            client.onUnmount(id)
        }
    }
}

val LocalSwrClient = staticCompositionLocalOf<SwrClient> {
    error("CompositionLocal 'SwrClient' not present")
}
