// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

@file: Suppress("unused")
package soil.query.compose.runtime

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.IdlingResource
import soil.query.test.TestSwrClient
import soil.query.test.TestSwrClientPlus

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.useIdlingResource(client: TestSwrClient): () -> Unit {
    val idlingResource = object : IdlingResource {
        override val isIdleNow: Boolean
            get() = client.isIdleNow()
    }
    registerIdlingResource(idlingResource)
    return {
        unregisterIdlingResource(idlingResource)
    }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.useIdlingResource(client: TestSwrClientPlus): () -> Unit {
    val idlingResource = object : IdlingResource {
        override val isIdleNow: Boolean
            get() = client.isIdleNow()
    }
    registerIdlingResource(idlingResource)
    return {
        unregisterIdlingResource(idlingResource)
    }
}
