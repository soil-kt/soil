// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules

fun <V> createTestRule(block: ValidationRuleBuilder<V>.() -> Unit): ValidationRule<V> {
    val allRules = rules(block)
    return { value ->
        val errorMessages = allRules.flatMap { rule ->
            when (val result = rule.invoke(value)) {
                is ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.messages
            }
        }
        if (errorMessages.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errorMessages)
    }
}
