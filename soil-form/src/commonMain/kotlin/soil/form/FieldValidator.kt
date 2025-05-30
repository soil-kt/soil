// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.form.core.ValidationResult
import soil.form.core.ValidationRuleBuilder
import soil.form.core.ValidationRuleSet
import soil.form.core.rules

typealias FieldValidator<V> = (V) -> FieldError

inline fun <V> FieldValidator(
    noinline block: ValidationRuleBuilder<V>.() -> Unit
): FieldValidator<V> = { value -> validate(value, rules(block)) }

fun <V> validate(value: V, rules: ValidationRuleSet<V>): FieldError {
    val errorMessages = rules.flatMap { rule ->
        when (val result = rule.invoke(value)) {
            is ValidationResult.Valid -> emptyList()
            is ValidationResult.Invalid -> result.messages
        }
    }
    return if (errorMessages.isEmpty()) noFieldError else FieldError(errorMessages)
}
