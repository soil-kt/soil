// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.text

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.form.FieldName
import soil.form.FieldValidator
import soil.form.compose.Form
import soil.form.compose.FormMetaState
import soil.form.compose.FormState
import soil.form.compose.rememberFormMetaState

/**
 * Converts a TextFieldState into a FormState for single-field form management.
 *
 * This extension function allows you to treat a single TextFieldState as a complete form,
 * enabling validation, submission handling, and state management for standalone text fields.
 * This is particularly useful for simple forms with a single text input, such as search bars,
 * comment fields, or single-field dialogs.
 *
 * Usage:
 * ```kotlin
 * val emailState = rememberTextFieldState()
 * val form = rememberForm(
 *     state = emailState.asFormState(),
 *     onSubmit = { state ->
 *         // Handle submission with state.text
 *         sendEmail(state.text.toString())
 *     }
 * )
 *
 * form.Field(
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     },
 *     render = { field ->
 *         TextField(
 *             state = field.state,
 *             isError = field.hasError
 *         )
 *     }
 * )
 * ```
 *
 * @param meta The form metadata state for tracking validation and submission status.
 * @return A FormState wrapping this TextFieldState for use with the Form composable.
 */
@Composable
fun TextFieldState.asFormState(
    meta: FormMetaState = rememberFormMetaState()
): FormState<TextFieldState> = remember(meta.key) {
    FormState(value = this, meta = meta)
}

/**
 * Creates a field for a TextFieldState-based form with direct text validation.
 *
 * This specialized Field function is designed for forms where the entire form data
 * is a single TextFieldState. It provides a streamlined API for single-field forms,
 * eliminating the need for selector and updater functions since the form data itself
 * is the TextFieldState.
 *
 * @param validator Optional validator for [TextFieldState.text].
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun Form<TextFieldState>.Field(
    validator: FieldValidator<CharSequence>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    render: @Composable (FormTextField) -> Unit
) {
    val control = rememberField(
        selector = { it },
        validator = validator,
        name = name,
        enabled = enabled
    )
    render(control)
}

/**
 * Creates a field for a TextFieldState-based form with type adaptation and validation.
 *
 * This overload allows you to use a TextFieldStateAdapter to convert the text content
 * to a different type for validation. This is useful for single-field forms that need
 * to validate the input as a specific data type while maintaining the simplicity of
 * a TextFieldState-only form.
 *
 * @param S The type used for validation after adaptation.
 * @param adapter The adapter that converts text content to the validation type.
 * @param validator Optional validator for the adapted type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun <S> Form<TextFieldState>.Field(
    adapter: TextFieldStateAdapter<S>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    render: @Composable (FormTextField) -> Unit
) {
    val control = rememberField(
        selector = { it },
        adapter = adapter,
        validator = validator,
        name = name,
        enabled = enabled
    )
    render(control)
}
