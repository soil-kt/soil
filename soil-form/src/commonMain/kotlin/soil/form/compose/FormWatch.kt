// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import soil.form.FormData

@Composable
fun <T, R> Form<T>.watch(
    calculation: FormData<T>.() -> R,
): R {
    val state = remember { derivedStateOf { state.calculation() } }
    return state.value
}
