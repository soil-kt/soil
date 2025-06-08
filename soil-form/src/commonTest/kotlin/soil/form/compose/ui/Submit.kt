// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.ui

import androidx.compose.foundation.focusable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import soil.form.compose.Form

@Composable
fun Form<*>.Submit(
    modifier: Modifier = Modifier
) {
    Button(
        onClick = ::handleSubmit,
        enabled = state.meta.canSubmit,
        modifier = modifier.focusable().testTag("submit")
    ) {
        Text("Submit")
    }
}
