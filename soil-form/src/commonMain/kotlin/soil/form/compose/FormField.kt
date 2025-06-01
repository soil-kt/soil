// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldPassthroughAdapter
import soil.form.FieldTypeAdapter
import soil.form.FieldValidator


/**
 * Creates a form field with validation and state management.
 *
 * This function creates a form field that automatically handles value changes,
 * validation, and state management. The field integrates with the parent form
 * and participates in form-wide validation.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.email },
 *     updater = { copy(email = it) },
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     }
 * ) { fieldControl ->
 *     TextField(
 *         value = fieldControl.value,
 *         onValueChange = fieldControl::onValueChange,
 *         isError = fieldControl.hasError
 *     )
 * }
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param validator Optional validator for the field value.
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param content The composable content that renders the field UI.
 */
@Composable
fun <T, V> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    validator: FieldValidator<V>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    content: @Composable (FormFieldControl<V>) -> Unit
) {
    val control = rememberFieldControl(
        selector = selector,
        updater = updater,
        adapter = FieldPassthroughAdapter(),
        validator = validator,
        name = name ?: auto,
        dependsOn = dependsOn.orEmpty(),
        enabled = enabled
    )
    content(control)
}

/**
 * Creates a form field with type adaptation, validation, and state management.
 *
 * This overload allows you to use a type adapter to convert between the stored
 * value type (V), validation target type (S), and input type (U). This is useful
 * when you need to validate or display a field value in a different format than
 * how it's stored.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.age },
 *     updater = { copy(age = it) },
 *     adapter = IntStringAdapter(),
 *     validator = FieldValidator<Int> {
 *         min(0) { "Age must be non-negative" }
 *         max(150) { "Age must be realistic" }
 *     }
 * ) { fieldControl ->
 *     TextField(
 *         value = fieldControl.value, // String type
 *         onValueChange = fieldControl::onValueChange
 *     )
 * }
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value as stored in the form data.
 * @param S The type used for validation.
 * @param U The type used for input/display.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param adapter The type adapter that converts between V, S, and U types.
 * @param validator Optional validator for the validation target type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param content The composable content that renders the field UI.
 */
@Composable
fun <T, V, S, U> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    content: @Composable (FormFieldControl<U>) -> Unit
) {
    val control = rememberFieldControl(
        selector = selector,
        updater = updater,
        adapter = adapter,
        validator = validator,
        name = name ?: auto,
        dependsOn = dependsOn.orEmpty(),
        enabled = enabled
    )
    content(control)
}

private val auto: FieldName
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return "field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
