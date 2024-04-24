// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias LongRule = ValidationRule<Long>
typealias LongRuleBuilder = ValidationRuleBuilder<Long>

class LongRuleTester(
    val predicate: Long.() -> Boolean,
    val message: () -> String
) : LongRule {
    override fun test(value: Long): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun LongRuleBuilder.minimum(limit: Long, message: () -> String) {
    extend(LongRuleTester({ this >= limit }, message))
}

fun LongRuleBuilder.maximum(limit: Long, message: () -> String) {
    extend(LongRuleTester({ this <= limit }, message))
}
