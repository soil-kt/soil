// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.text

import androidx.compose.foundation.text.input.TextFieldState
import soil.form.FieldTypeAdapter

/**
 * An adapter interface specifically designed for TextFieldState type conversion.
 *
 * This specialized adapter works with Compose's TextFieldState, maintaining the state
 * object for input/display while allowing conversion to a different type for validation.
 * The stored value and input types are always TextFieldState, but the validation target
 * type can be customized.
 *
 * This is particularly useful when you need to validate the text content as a specific
 * type (e.g., numbers, dates, emails) while keeping the TextFieldState for UI interaction.
 *
 * Usage:
 * ```kotlin
 * class IntTextFieldStateAdapter : TextFieldStateAdapter<Int?> {
 *     override fun toValidationTarget(value: TextFieldState): Int? {
 *         return value.text.toString().toIntOrNull()
 *     }
 * }
 * ```
 *
 * @param S The type used for validation (e.g., Int, Date, or custom types).
 */
interface TextFieldStateAdapter<S> : FieldTypeAdapter<TextFieldState, S, TextFieldState> {
    override fun fromInput(value: TextFieldState, current: TextFieldState): TextFieldState = value
    override fun toInput(value: TextFieldState): TextFieldState = value
}

/**
 * A default adapter that validates [TextFieldState.text] as CharSequence.
 *
 * This adapter extracts the text content from TextFieldState for validation
 * while maintaining the TextFieldState for UI interaction. It's the simplest
 * adapter that allows direct text validation without type conversion.
 *
 * This is the default adapter used when no explicit adapter is provided
 * to a TextFieldState-based form field.
 */
class TextFieldPassthroughAdapter : TextFieldStateAdapter<CharSequence> {
    override fun toValidationTarget(value: TextFieldState): CharSequence = value.text
}
