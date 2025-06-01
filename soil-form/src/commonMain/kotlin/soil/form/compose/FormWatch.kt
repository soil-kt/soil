// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import soil.form.FormData

/**
 * Watches for changes in form state and recomputes a derived value.
 *
 * This function uses `derivedStateOf` internally, which can be expensive for frequent recompositions.
 * It's primarily intended for cases where you need to synchronously update field states based on
 * input values, such as enabling/disabling fields based on other field values.
 *
 * For simple conditions with infrequent recompositions, consider using the form state directly
 * instead of this watch function to avoid unnecessary overhead.
 *
 * Usage:
 * ```kotlin
 * // Enable password confirmation field only when password is not blank
 * val isPasswordConfirmationEnabled = form.watch { value.password.isNotBlank() }
 * form.PasswordConfirm(
 *     name = "pwd-cfm",
 *     dependsOn = setOf("pwd"),
 *     enabled = isPasswordConfirmationEnabled
 * ) {
 *     // field implementation
 * }
 *
 * // Enable submit button based on multiple field conditions
 * val canSubmitForm = form.watch {
 *     value.email.isNotBlank() && value.password.length >= 8
 * }
 *
 * // Show conditional fields based on user selection
 * val showAdvancedOptions = form.watch { value.userType == "advanced" }
 * ```
 *
 * @param T The type of the form data.
 * @param R The type of the computed result.
 * @param calculation A function that computes a value based on the current form state.
 * @return The computed value that updates when form state changes.
 */
@Composable
fun <T, R> Form<T>.watch(
    calculation: FormData<T>.() -> R,
): R {
    val state = remember { derivedStateOf { state.calculation() } }
    return state.value
}
