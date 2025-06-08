// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds

class FormOptionsTest : UnitTest() {

    @Test
    fun factory_default() {
        val actual = FormOptions()
        assertEquals(FormOptions.Default.preValidation, actual.preValidation)
        assertEquals(FormOptions.Default.preValidationDelayOnMount, actual.preValidationDelayOnMount)
        assertEquals(FormOptions.Default.preValidationDelayOnChange, actual.preValidationDelayOnChange)
    }

    @Test
    fun factory_specifyingArguments() {
        val actual = FormOptions(
            preValidation = false,
            preValidationDelayOnMount = 500.milliseconds,
            preValidationDelayOnChange = 300.milliseconds
        )

        assertNotEquals(FormOptions.Default.preValidation, actual.preValidation)
        assertNotEquals(FormOptions.Default.preValidationDelayOnMount, actual.preValidationDelayOnMount)
        assertNotEquals(FormOptions.Default.preValidationDelayOnChange, actual.preValidationDelayOnChange)
    }
}
