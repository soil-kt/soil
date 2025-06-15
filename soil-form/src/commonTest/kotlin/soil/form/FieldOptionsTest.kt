// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds

class FieldOptionsTest : UnitTest() {

    @Test
    fun factory_default() {
        val actual = FieldOptions()
        assertEquals(FieldOptions.Default.validationStrategy, actual.validationStrategy)
        assertEquals(FieldOptions.Default.validationDelayOnMount, actual.validationDelayOnMount)
        assertEquals(FieldOptions.Default.validationDelayOnChange, actual.validationDelayOnChange)
        assertEquals(FieldOptions.Default.validationDelayOnBlur, actual.validationDelayOnBlur)
    }

    @Test
    fun factory_specifyingArguments() {
        val customStrategy = FieldValidationStrategy(
            initial = FieldValidationMode.Change,
            next = { _, _ -> FieldValidationMode.Submit }
        )

        val actual = FieldOptions(
            validationStrategy = customStrategy,
            validationDelayOnMount = 100.milliseconds,
            validationDelayOnChange = 500.milliseconds,
            validationDelayOnBlur = 200.milliseconds
        )

        assertNotEquals(FieldOptions.Default.validationStrategy, actual.validationStrategy)
        assertNotEquals(FieldOptions.Default.validationDelayOnMount, actual.validationDelayOnMount)
        assertNotEquals(FieldOptions.Default.validationDelayOnChange, actual.validationDelayOnChange)
        assertNotEquals(FieldOptions.Default.validationDelayOnBlur, actual.validationDelayOnBlur)
    }
}
