// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.text

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import soil.form.FieldError
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.annotation.InternalSoilFormApi
import soil.form.compose.FormController
import soil.form.compose.FormState
import soil.form.compose.hasError
import soil.form.noFieldError
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(InternalSoilFormApi::class)
class FormTextFieldTest : UnitTest() {

    @Test
    fun testInitialization() {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
            validator = null,
            name = "testField",
            dependsOn = emptySet()
        )

        assertEquals("testField", controller.name)
        assertEquals(textFieldState, controller.state)
        assertEquals("", controller.state.text.toString())
        assertEquals(noFieldError, controller.error)
        assertFalse(controller.hasError)
        assertFalse(controller.isTouched)
        assertFalse(controller.isFocused)
        assertTrue(controller.isEnabled)
    }

    @Test
    fun testTextFieldStateChanges() {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()

        textFieldState.setTextAndPlaceCursorAtEnd("John Doe")
        assertEquals("John Doe", controller.state.text.toString())
        assertEquals("John Doe", controller.validationTarget.toString())
    }

    @Test
    fun testFocusHandling() {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val validator: FieldValidator<CharSequence> = { value ->
            if (value.isBlank()) FieldError("Name is required") else noFieldError
        }

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        textFieldState.setTextAndPlaceCursorAtEnd("John")
        controller.trigger(FieldValidationMode.Change)
        assertFalse(controller.hasError)
        assertEquals(noFieldError, controller.error)
    }

    @Test
    fun testValidationWithoutValidator() {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
            validator = { value: CharSequence ->
                if (value.isBlank()) FieldError("must be not blank") else noFieldError
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
        val textFieldState = TextFieldState()
        textFieldState.setTextAndPlaceCursorAtEnd("25")

        val formState = FormState(value = TestFormData(ageState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.ageState },
            adapter = IntTextFieldStateAdapter(),
            validator = null,
            name = "age",
            dependsOn = emptySet()
        )

        assertEquals(25, controller.validationTarget)
        assertEquals("25", controller.state.text.toString())
    }

    @Test
    fun testErrorState() {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
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
        val nameState = TextFieldState()
        val ageState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = nameState, ageState = ageState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val nameController = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = setOf("age")
        ).also { it.register() }

        val emailController = FormTextFieldController(
            form = formController.binding,
            selector = { it.emailState },
            adapter = TextFieldPassthroughAdapter(),
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

    @Test
    fun testNotifyFormChange() = runTest {
        val textFieldState = TextFieldState()
        val formState = FormState(value = TestFormData(nameState = textFieldState))
        val formController = FormController(
            state = formState,
            onSubmit = {}
        )

        val controller = FormTextFieldController(
            form = formController.binding,
            selector = { it.nameState },
            adapter = TextFieldPassthroughAdapter(),
            validator = null,
            name = "name",
            dependsOn = emptySet()
        )

        controller.register()

        formController.binding.fieldChanges.test {
            controller.notifyFormChange()
            assertEquals("name", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    data class TestFormData(
        val nameState: TextFieldState = TextFieldState(),
        val ageState: TextFieldState = TextFieldState(),
        val emailState: TextFieldState = TextFieldState()
    )

    private class IntTextFieldStateAdapter : TextFieldStateAdapter<Int> {
        override fun toValidationTarget(value: TextFieldState): Int {
            return value.text.toString().toIntOrNull() ?: 0
        }
    }
}
