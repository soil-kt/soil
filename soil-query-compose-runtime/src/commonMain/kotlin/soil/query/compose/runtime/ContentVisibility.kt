// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser

// NOTE: Visibility must be treated similarly to Android View's invisible because it needs to receive the loading state from Await placed as a child.
// (If AnimatedVisibility's visible=false, the content isn't called while it's hidden, so Await won't function)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContentVisibility(
    hidden: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .alpha(if (hidden) 0f else 1f)
            .clearAndSetSemantics {
                if (hidden) {
                    invisibleToUser()
                }
            }
    ) {
        content()
    }
}
