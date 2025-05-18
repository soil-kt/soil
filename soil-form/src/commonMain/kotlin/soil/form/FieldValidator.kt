package soil.form

import soil.form.core.ValidationResult
import soil.form.core.ValidationRuleBuilder
import soil.form.core.ValidationRuleSet
import soil.form.core.rules

typealias FieldValidator<V> = (V) -> FieldErrors

inline fun <V> FieldValidator(
    noinline block: ValidationRuleBuilder<V>.() -> Unit
): FieldValidator<V> = { value -> validate(value, rules(block)) }

fun <V> validate(value: V, rules: ValidationRuleSet<V>): FieldErrors {
    return rules.flatMap { rule ->
        when (val result = rule.test(value)) {
            is ValidationResult.Valid -> noErrors
            is ValidationResult.Invalid -> result.errors
        }
    }
}
