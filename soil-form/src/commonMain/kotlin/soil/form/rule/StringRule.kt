// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias StringRule = ValidationRule<String>
typealias StringRuleBuilder = ValidationRuleBuilder<String>

class StringRuleTester(
    val predicate: String.() -> Boolean,
    val message: () -> String
) : StringRule {
    override fun test(value: String): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun StringRuleBuilder.notEmpty(message: () -> String) {
    extend(StringRuleTester(String::isNotEmpty, message))
}

fun StringRuleBuilder.notBlank(message: () -> String) {
    extend(StringRuleTester(String::isNotBlank, message))
}

fun StringRuleBuilder.minLength(limit: Int, message: () -> String) {
    extend(StringRuleTester({ length >= limit }, message))
}

fun StringRuleBuilder.maxLength(limit: Int, message: () -> String) {
    extend(StringRuleTester({ length <= limit }, message))
}

fun StringRuleBuilder.pattern(pattern: String, message: () -> String) {
    pattern(Regex(pattern), message)
}

fun StringRuleBuilder.pattern(pattern: Regex, message: () -> String) {
    extend(StringRuleTester({ pattern.matches(this) }, message))
}
