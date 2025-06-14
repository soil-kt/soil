// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.tooling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.form.FieldName
import soil.form.FieldOptions
import soil.form.FieldTypeAdapter
import soil.form.FieldValidationMode
import soil.form.FieldValidationStrategy
import soil.form.FieldValidator
import soil.form.compose.Field
import soil.form.compose.FormController
import soil.form.compose.FormField
import soil.form.compose.FormPolicy
import soil.form.compose.FormState

@Composable
fun <V> PreviewField(
    initialValue: V,
    validator: FieldValidator<V>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    render: @Composable (FormField<V>) -> Unit
) {
    val form = remember { FormController(FormState(initialValue, previewPolicy)) { /* no-op */ } }
    form.Field(
        selector = { it },
        updater = { it },
        validator = validator,
        name = name,
        enabled = enabled,
        render = render
    )
}

@Composable
fun <V, S, U> PreviewField(
    initialValue: V,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    enabled: Boolean = true,
    render: @Composable (FormField<U>) -> Unit
) {
    val form = remember { FormController(FormState(initialValue, previewPolicy)) { /* no-op */ } }
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

private val previewPolicy: FormPolicy
    get() = FormPolicy(
        fieldOptions = FieldOptions(FieldValidationStrategy(initial = FieldValidationMode.Change))
    )
