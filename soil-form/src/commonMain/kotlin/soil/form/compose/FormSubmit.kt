// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import soil.form.Submission

@Composable
fun <T : Any> FormScope<T>.Submit(
    content: @Composable (Submission) -> Unit
) {
    Controller(
        control = rememberSubmissionControl(),
        content = content
    )
}
