// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FieldErrorTest : UnitTest() {

    @Test
    fun testEquality() {
        assertNotEquals(noFieldError, FieldError("Invalid!"))
        assertEquals(noFieldError, FieldError(emptyList()))
        assertEquals(noFieldError, FieldError(listOf()))
    }
}
