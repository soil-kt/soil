// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors
import soil.form.rules

typealias ObjectRule<V> = ValidationRule<V>
typealias ObjectRuleBuilder<V> = ValidationRuleBuilder<V>

class ObjectRuleTester<V>(
    val predicate: V.() -> Boolean,
    val message: () -> String
) : ObjectRule<V> {
    override fun test(value: V): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

class ObjectRuleChainer<V, S>(
    val transform: (V) -> S
) : ObjectRule<V> {

    private var ruleSet: Set<ValidationRule<S>> = emptySet()

    override fun test(value: V): FieldErrors {
        return transform(value).let { ruleSet.flatMap { rule -> rule.test(it) } }
    }

    infix fun then(block: ValidationRuleBuilder<S>.() -> Unit) {
        ruleSet = rules(block)
    }
}

fun <V : Any> ObjectRuleBuilder<V>.test(predicate: V.() -> Boolean, message: () -> String) {
    extend(ObjectRuleTester(predicate, message))
}

fun <V : Any, S> ObjectRuleBuilder<V>.cast(transform: (V) -> S): ObjectRuleChainer<V, S> {
    return ObjectRuleChainer(transform).also { extend(it) }
}
