// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Represents a rule that can be applied to a form field.
 */
fun interface FormRule<T> {

    /**
     * Tests the given value against the rule.
     *
     * @param value The value to test.
     * @param dryRun Whether to perform the test without side effects.
     * @return `true` if the value passes the rule; `false` otherwise.
     */
    fun test(value: T, dryRun: Boolean): Boolean
}
