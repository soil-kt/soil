// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.tooling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.form.FieldName
import soil.form.FieldTypeAdapter
import soil.form.FieldValidator
import soil.form.compose.Field
import soil.form.compose.FormController
import soil.form.compose.FormField
import soil.form.compose.FormState

/**
 * Creates a standalone form field for use in Compose Previews and testing.
 *
 * This composable allows you to preview and test individual form fields without
 * creating a full form context. It's particularly useful for developing field
 * components in isolation using Compose `@Preview` functionality.
 *
 * The field operates with a minimal form controller that handles validation and
 * state management, but doesn't perform any actual form submission or data persistence.
 *
 * Usage in a Compose Preview:
 * ```kotlin
 * @Preview(showBackground = true)
 * @Composable
 * private fun InputFieldPreview() {
 *     PreviewField(
 *         initialValue = "",
 *         validator = FieldValidator {
 *             notBlank { "This field cannot be blank" }
 *         },
 *         render = { field ->
 *             TextField(
 *                 value = field.value,
 *                 onValueChange = field::onValueChange,
 *                 isError = field.hasError,
 *                 label = { Text("Name") }
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * @param V The type of the field value.
 * @param initialValue The initial value for the field.
 * @param validator Optional validator to test field validation behavior.
 * @param name Optional field name. If null, an auto-generated name is used.
 * @param enabled Whether the field should be enabled for interaction.
 * @param policy The preview policy that controls validation behavior and timing.
 * @param render The composable content that renders the field UI using the provided FormField.
 */
@Composable
fun <V> PreviewField(
    initialValue: V,
    validator: FieldValidator<V>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    policy: PreviewPolicy = defaultPreviewPolicy,
    render: @Composable (FormField<V>) -> Unit
) {
    val form = remember { FormController(FormState(initialValue, policy)) { /* no-op */ } }
    form.Field(
        selector = { it },
        updater = { it },
        validator = validator,
        name = name,
        enabled = enabled,
        render = render
    )
}

/**
 * Creates a standalone form field with type adaptation for use in Compose Previews and testing.
 *
 * This overload allows you to preview fields that use type adapters to convert between
 * different representations of the same data (stored value, validation target, and input type).
 * This is useful for testing fields that display data in a different format than how it's stored.
 *
 * The field operates with a minimal form controller that handles validation and
 * state management, but doesn't perform any actual form submission or data persistence.
 *
 * Usage in a Compose Preview:
 * ```kotlin
 * @Preview(showBackground = true)
 * @Composable
 * private fun AgeFieldPreview() {
 *     PreviewField(
 *         initialValue = 25, // Stored as Int
 *         adapter = IntStringAdapter(), // Converts Int <-> String
 *         validator = FieldValidator<Int> {
 *             min(0) { "Age must be non-negative" }
 *             max(150) { "Age must be realistic" }
 *         },
 *         render = { field ->
 *             TextField(
 *                 value = field.value, // String type from adapter
 *                 onValueChange = field::onValueChange,
 *                 isError = field.hasError,
 *                 label = { Text("Age") }
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * @param V The type of the field value as stored.
 * @param S The type used for validation.
 * @param U The type used for input/display in the UI.
 * @param initialValue The initial value for the field (in stored type V).
 * @param adapter The type adapter that converts between V, S, and U types.
 * @param validator Optional validator for the validation target type (S).
 * @param name Optional field name. If null, an auto-generated name is used.
 * @param enabled Whether the field should be enabled for interaction.
 * @param policy The preview policy that controls validation behavior and timing.
 * @param render The composable content that renders the field UI using the provided FormField of type U.
 */
@Composable
fun <V, S, U> PreviewField(
    initialValue: V,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    policy: PreviewPolicy = defaultPreviewPolicy,
    render: @Composable (FormField<U>) -> Unit
) {
    val form = remember { FormController(FormState(initialValue, policy)) { /* no-op */ } }
    form.Field(
        selector = { it },
        updater = { it },
        adapter = adapter,
        validator = validator,
        name = name,
        enabled = enabled,
        render = render
    )
}

private val defaultPreviewPolicy: PreviewPolicy = PreviewPolicy()
