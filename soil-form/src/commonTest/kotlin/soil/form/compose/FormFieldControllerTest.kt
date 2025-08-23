// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import soil.form.FieldError
import soil.form.FieldPassthroughAdapter
import soil.form.FieldTypeAdapter
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.annotation.InternalSoilFormApi
import soil.form.noFieldError
import soil.form.rule.notBlank
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(InternalSoilFormApi::class)
class FormFieldControllerTest : UnitTest() {

    @Test
    fun testInitialization() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "testField",
            dependsOn = emptySet()
        )

        assertEquals("testField", controller.name)
        assertEquals("", controller.value)
        assertEquals(noFieldError, controller.error)
        assertFalse(controller.hasError)
        assertFalse(controller.isTouched)
        assertFalse(controller.isFocused)
        assertTrue(controller.isEnabled)
    }

    @Test
    fun testValueChange() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()
        controller.onValueChange("John")

        assertEquals("John", formState.value.name)
        assertEquals("John", controller.value)
    }

    @Test
    fun testValueChangeWithAdapter() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.age },
            updater = { copy(age = it) },
            adapter = IntStringAdapter(),
            validator = null,
            name = "age",
            dependsOn = emptySet()
        )

        controller.register()
        controller.onValueChange("25")

        assertEquals(25, formState.value.age)
        assertEquals("25", controller.value)
    }

    @Test
    fun testFocusHandling() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        // Test onFocus
        controller.onFocus()
        assertTrue(controller.isFocused)

        // Test onBlur
        controller.onBlur()
        assertFalse(controller.isFocused)
        assertTrue(controller.isTouched)
    }

    @Test
    fun testHandleFocus() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        // Test gaining focus
        controller.handleFocus(true)
        assertTrue(controller.isFocused)

        // Test losing focus
        controller.handleFocus(false)
        assertFalse(controller.isFocused)
        assertTrue(controller.isTouched)

        // Test no change when already focused
        controller.isFocused = true
        controller.isTouched = false
        controller.handleFocus(true)
        assertTrue(controller.isFocused)
        assertFalse(controller.isTouched)
    }

    @Test
    fun testValidationWithValidator() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val validator: FieldValidator<String> = { value ->
            if (value.isBlank()) FieldError("Name is required") else noFieldError
        }

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = validator,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()

        // Test validation failure
        controller.trigger(FieldValidationMode.Blur)
        assertTrue(controller.hasError)
        assertEquals(listOf("Name is required"), controller.error.messages)

        // Test validation success
        controller.onValueChange("John")
        controller.trigger(FieldValidationMode.Change)
        assertFalse(controller.hasError)
        assertEquals(noFieldError, controller.error)
    }

    @Test
    fun testValidationWithoutValidator() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()
        controller.trigger(FieldValidationMode.Blur)

        assertFalse(controller.hasError)
        assertEquals(noFieldError, controller.error)
    }

    @Test
    fun testShouldTrigger() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()
        val meta = formController.binding["name"]!!

        // Test with matching mode
        meta.mode = FieldValidationMode.Blur
        assertTrue(controller.shouldTrigger(FieldValidationMode.Blur))

        // Test with non-matching mode
        assertFalse(controller.shouldTrigger(FieldValidationMode.Change))
    }

    @Test
    fun testRegisterAndUnregister() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        // Test register
        controller.register()
        assertTrue(formController.fields.contains("name"))
        assertTrue(formState.meta.fields.containsKey("name"))

        // Test unregister
        controller.unregister()
        assertFalse(formController.fields.contains("name"))
    }

    @Test
    fun testRevalidateIfNeeded() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = FieldValidator {
                notBlank { "must be not blank" }
            },
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()

        val fieldState = checkNotNull(formState.meta.fields["name"])

        assertFalse(fieldState.isValidated)
        assertEquals(noFieldError, fieldState.error)

        controller.revalidateIfNeeded()

        assertFalse(fieldState.isValidated)
        assertEquals(noFieldError, fieldState.error)

        fieldState.isValidated = true
        controller.revalidateIfNeeded()

        assertNotEquals(noFieldError, fieldState.error)
        assertTrue(controller.hasError)
        assertTrue(fieldState.isValidated)
    }

    @Test
    fun testValidationTargetWithAdapter() {
        val formState = FormState(value = TestFormData(age = 25))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.age },
            updater = { copy(age = it) },
            adapter = IntStringAdapter(),
            validator = null,
            name = "age",
            dependsOn = emptySet()
        )

        assertEquals("25", controller.validationTarget)
        assertEquals("25", controller.value)
    }

    @Test
    fun testErrorState() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()

        // Test no error initially
        assertFalse(controller.hasError)

        // Test setting error
        controller.error = FieldError("Test error")
        assertTrue(controller.hasError)
        assertEquals(listOf("Test error"), controller.error.messages)
    }

    @Test
    fun testEnabledState() {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        // Test default enabled state
        assertTrue(controller.isEnabled)

        // Test disabling
        controller.isEnabled = false
        assertFalse(controller.isEnabled)
    }

    @Test
    fun testDependentFieldChanges() = runTest {
        val formState = FormState(value = TestFormData())
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val nameController = FormFieldController(
            form = formController.binding,
            selector = { it.name },
            updater = { copy(name = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = setOf("age")
        ).also { it.register() }

        val emailController = FormFieldController(
            form = formController.binding,
            selector = { it.email },
            updater = { copy(email = it) },
            adapter = FieldPassthroughAdapter(),
            validator = null,
            name = "email",
            dependsOn = emptySet()
        ).also { it.register() }

        // Test nameController receives notifications for fields it depends on
        nameController.dependentFieldChanges.test {
            // Emit field changes after starting the test
            formController.binding.notifyFieldChange("age")
            assertEquals("age", awaitItem())

            formController.binding.notifyFieldChange("age")
            assertEquals("age", awaitItem())

            // This should not be received since "name" is not in dependsOn
            formController.binding.notifyFieldChange("name")
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }

        // Test emailController doesn't receive any notifications since it has no dependencies
        emailController.dependentFieldChanges.test {
            // Emit various field changes
            formController.binding.notifyFieldChange("age")
            formController.binding.notifyFieldChange("name")
            formController.binding.notifyFieldChange("email")

            // Should not receive any events since dependsOn is empty
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Test data class
    data class TestFormData(
        val name: String = "",
        val age: Int = 0,
        val email: String = ""
    )

    // Test adapter for Int <-> String conversion
    private class IntStringAdapter : FieldTypeAdapter<Int, String, String> {
        override fun toValidationTarget(value: Int): String = value.toString()
        override fun toInput(value: Int): String = value.toString()
        override fun fromInput(value: String, current: Int): Int = value.toIntOrNull() ?: current
    }
}
