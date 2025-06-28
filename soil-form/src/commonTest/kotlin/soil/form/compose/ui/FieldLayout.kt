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
import soil.form.compose.FormField
import soil.form.compose.hasError

@Composable
fun <V> FieldLayout(
    field: FormField<V>,
    modifier: Modifier = Modifier,
    content: @Composable FormField<V>.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        field.content()
        if (field.isEnabled && field.hasError) {
            FieldValidationError(
                text = field.error.messages.first(),
                modifier = Modifier.padding(horizontal = 16.dp).testTag("${field.name}_error")
            )
        }
    }
}
