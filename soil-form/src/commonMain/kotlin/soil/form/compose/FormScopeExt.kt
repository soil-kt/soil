// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.ValidationRuleBuilder
import soil.form.ValidationRuleSet
import soil.form.rules

@Composable
fun <T : Any, V> FormScope<T>.rememberFieldRuleControl(
    name: FieldName,
    select: T.() -> V,
    update: T.(V) -> T,
    enabled: T.() -> Boolean = { true },
    dependsOn: Set<FieldName>? = null,
    builder: ValidationRuleBuilder<V>.() -> Unit
): FieldControl<V> {
    return rememberFieldRuleControl(
        name = name,
        select = select,
        update = update,
        enabled = enabled,
        dependsOn = dependsOn,
        ruleSet = remember(formState) { rules(builder) }
    )
}

@Composable
fun <T : Any, V> FormScope<T>.rememberFieldRuleControl(
    name: FieldName,
    select: T.() -> V,
    update: T.(V) -> T,
    enabled: T.() -> Boolean = { true },
    dependsOn: Set<FieldName>? = null,
    ruleSet: ValidationRuleSet<V>,
): FieldControl<V> {
    val handleValidate = remember<T.() -> FieldErrors?>(formState) {
        {
            val currentValue = select()
            ruleSet.flatMap { it.test(currentValue) }
        }
    }
    return rememberFieldControl(
        name = name,
        select = select,
        update = update,
        enabled = enabled,
        dependsOn = dependsOn,
        validate = handleValidate,
    )
}

@Composable
fun <T : Any> FormScope<T>.rememberSubmissionRuleAutoControl(): SubmissionControl<T> {
    return rememberSubmissionControl(validate = { rules, dryRun ->
        if (dryRun) {
            rules.values.all { it.test(this, dryRun = true) }
        } else {
            // NOTE: Related to FieldValidateOn, make sure to traverse all rules
            rules.values.map { it.test(this, dryRun = false) }.all { it }
        }
    })
}

@Composable
fun <T : Any, V> FormScope<T>.rememberWatch(select: T.() -> V): V {
    val value by remember { derivedStateOf { with(formState.value) { select() } } }
    return value
}
