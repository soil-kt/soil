// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias IntRule = ValidationRule<Int>
typealias IntRuleBuilder = ValidationRuleBuilder<Int>

class IntRuleTester(
    val predicate: Int.() -> Boolean,
    val message: () -> String
) : IntRule {
    override fun test(value: Int): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun IntRuleBuilder.minimum(limit: Int, message: () -> String) {
    extend(IntRuleTester({ this >= limit }, message))
}

fun IntRuleBuilder.maximum(limit: Int, message: () -> String) {
    extend(IntRuleTester({ this <= limit }, message))
}
