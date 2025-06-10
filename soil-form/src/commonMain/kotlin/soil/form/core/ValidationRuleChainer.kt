// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

fun interface ValidationRuleChainer<V> {
    infix fun then(block: ValidationRuleBuilder<V>.() -> Unit)
}
