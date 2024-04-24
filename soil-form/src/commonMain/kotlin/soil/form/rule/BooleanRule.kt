// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias BooleanRule = ValidationRule<Boolean>
typealias BooleanRuleBuilder = ValidationRuleBuilder<Boolean>

class BooleanRuleTester(
    val predicate: Boolean.() -> Boolean,
    val message: () -> String
) : BooleanRule {
    override fun test(value: Boolean): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun BooleanRuleBuilder.isTrue(message: () -> String) {
    extend(BooleanRuleTester({ this }, message))
}

fun BooleanRuleBuilder.isFalse(message: () -> String) {
    extend(BooleanRuleTester({ !this }, message))
}
