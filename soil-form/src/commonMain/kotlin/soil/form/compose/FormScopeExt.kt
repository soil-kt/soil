// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.core.ValidationResult
import soil.form.core.ValidationRuleBuilder
import soil.form.core.ValidationRuleSet
import soil.form.core.rules

/**
 * Remembers a field control for the given field name with the given rule set.
 *
 * Usage:
 * ```kotlin
 * rememberFieldRuleControl(
 *     name = "First name",
 *     select = { firstName },
 *     update = { copy(firstName = it) }
 * ) {
 *     notBlank { "must be not blank" }
 * }
 * ```
 *
 * @param T The type of the form value.
 * @param V The type of the field value.
 * @param name The name of the field.
 * @param select The function to select the field value.
 * @param update The function to update the field value.
 * @param enabled The function to determine if the field is enabled.
 * @param dependsOn The set of field names that this field depends on.
 * @param builder The block to build the rule set.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
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

/**
 * Remembers a field control for the given field name with the given rule set.
 *
 * Usage:
 * ```kotlin
 * val ruleSet = rules<String> {
 *     notBlank { "must be not blank" }
 * }
 *
 * rememberFieldRuleControl(
 *     name = "First name",
 *     select = { firstName },
 *     update = { copy(firstName = it) },
 *     ruleSet = ruleSet
 * )
 * ```
 *
 * @param T The type of the form value.
 * @param V The type of the field value.
 * @param name The name of the field.
 * @param select The function to select the field value.
 * @param update The function to update the field value.
 * @param enabled The function to determine if the field is enabled.
 * @param dependsOn The set of field names that this field depends on.
 * @param ruleSet The rule set to validate the field value.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
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
            ruleSet.flatMap {
                when (val result = it.invoke(currentValue)) {
                    is ValidationResult.Valid -> emptyList()
                    is ValidationResult.Invalid -> result.messages
                }
            }
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

/**
 * Remembers a submission rule control that automatically controls state of the form.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
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

/**
 * Remembers a watch value that automatically updates when the form state changes.
 *
 * @param T The type of the form value.
 * @param V The type of the watch value.
 * @param select The function to select the watch value.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
@Composable
fun <T : Any, V> FormScope<T>.rememberWatch(select: T.() -> V): V {
    val value by remember { derivedStateOf { with(formState.value) { select() } } }
    return value
}
