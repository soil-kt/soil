// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import soil.form.Field
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.ValidationRuleSet

@Composable
fun <T : Any, V> FormScope<T>.Field(
    value: (T) -> V,
    // TODO: (V) -> Unit
    //  state.update { }
    onChange: T.(V) -> T,
    rules: ValidationRuleSet<V> = emptySet(),
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    content: @Composable (Field<V>) -> Unit
) {
    Controller(
        control = rememberFieldRuleControl(
            name = name ?: auto,
            select = { value(this) },
            update = onChange,
            enabled = { enabled },
            dependsOn = dependsOn,
            ruleSet = rules,
        ),
        content = content
    )
}

private val auto: FieldName
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return "field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
