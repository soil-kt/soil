// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import soil.form.annotation.InternalSoilFormApi
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormControllerTest : UnitTest() {

    @Test
    fun testSubmit() {
        var submittedData: TestData? = null
        val formState = FormState(value = TestData())
        val formController = FormController(
            state = formState,
            onSubmit = { submittedData = it }
        )

        formController.handleSubmit()
        assertEquals(TestData(), submittedData)
    }

    @Test
    fun testSubmit_validationError() {
        var submittedData: TestData? = null
        val formState = FormState(value = TestData())
        val formController = FormController(
            state = formState,
            onSubmit = { submittedData = it }
        )
        formController.register("firstName") { _, _ ->
            false // Simulate validation error
        }

        formController.handleSubmit()
        assertNull(submittedData)
    }

    @Test
    fun testPreValidate() {
        val formState = FormState(value = TestData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        formController.preValidate(TestData(firstName = "John", lastName = "Doe"))
        assertTrue(formState.meta.canSubmit)
    }

    @Test
    fun testPreValidate_validationError() {
        val formState = FormState(value = TestData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        formController.register("firstName") { _, _ ->
            false // Simulate validation error
        }

        formController.preValidate(TestData(firstName = "John", lastName = "Doe"))
        assertFalse(formState.meta.canSubmit)
    }

    @OptIn(InternalSoilFormApi::class)
    @Test
    fun testValidate() {
        val formState = FormState(value = TestData())
        val binding = FormController(
            state = formState,
            onSubmit = {}
        ).binding

        var firstNameValidationCount = 0
        binding.register("firstName") { _, _ ->
            firstNameValidationCount++
            false // Simulate validation error
        }
        binding["firstName"] = FieldMetaState()

        var lastNameValidationCount = 0
        binding.register("lastName") { _, _ ->
            lastNameValidationCount++
            false // Simulate validation error
        }

        val actual = binding.validate(TestData(), dryRun = false)
        assertFalse(actual)
        assertEquals(1, firstNameValidationCount)
        assertEquals(1, lastNameValidationCount)
    }

    @OptIn(InternalSoilFormApi::class)
    @Test
    fun testValidate_withDryRun() {
        val formState = FormState(value = TestData())
        val binding = FormController(
            state = formState,
            onSubmit = {}
        ).binding

        var firstNameValidationCount = 0
        binding.register("firstName") { _, _ ->
            firstNameValidationCount++
            false // Simulate validation error
        }
        binding["firstName"] = FieldMetaState()

        var lastNameValidationCount = 0
        binding.register("lastName") { _, _ ->
            lastNameValidationCount++
            false // Simulate validation error
        }

        val actual = binding.validate(TestData(), dryRun = true)
        assertFalse(actual)
        assertEquals(1, firstNameValidationCount + lastNameValidationCount)
    }

    @OptIn(InternalSoilFormApi::class)
    @Test
    fun testChange() {
        val formState = FormState(value = TestData())
        val binding = FormController(
            state = formState,
            onSubmit = {}
        ).binding

        binding.handleChange { copy(firstName = "John") }
        assertEquals("John", formState.value.firstName)

        binding.handleChange { copy(lastName = "Doe") }
        assertEquals("Doe", formState.value.lastName)
    }

    data class TestData(
        val firstName: String = "",
        val lastName: String = ""
    )
}
