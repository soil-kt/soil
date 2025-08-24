// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import soil.form.compose.BasicFormField
import soil.form.compose.hasError

@Composable
inline fun <T : BasicFormField> T.WithLayout(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    content: @Composable T.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        content()
        if (isEnabled && hasError) {
            FieldValidationError(
                text = error.messages.first(),
                modifier = Modifier.padding(horizontal = 16.dp).testTag("${name}_error")
            )
        }
    }
}
