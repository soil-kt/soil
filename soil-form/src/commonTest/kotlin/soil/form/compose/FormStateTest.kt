// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.autoSaver
import soil.form.FieldError
import soil.form.FieldValidationMode
import soil.form.noFieldError
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormStateTest : UnitTest() {

    @Test
    fun testState() {
        val initialValue = TestData(firstName = "John", lastName = "Doe")
        val formState = FormState(value = initialValue)

        assertEquals(initialValue, formState.value)
        assertEquals(0, formState.meta.fields.size)
        assertEquals(0, formState.resetKey)
        assertFalse(formState.meta.canSubmit)
    }

    @Test
    fun testState_withMinimalPolicy() {
        val initialValue = TestData(firstName = "John", lastName = "Doe")
        val formState = FormState(value = initialValue, policy = FormPolicy.Minimal)

        assertEquals(initialValue, formState.value)
        assertTrue(formState.meta.canSubmit)
    }

    @Test
    fun testState_reset() {
        val initialValue = TestData(firstName = "John", lastName = "Doe")
        val formState = FormState(value = initialValue)

        // Add some field metadata to simulate form usage
        formState.meta.fields["firstName"] = FieldMetaState(
            error = FieldError("Some error"),
            mode = FieldValidationMode.Change,
            isDirty = true,
            isTouched = true,
            isValidated = true
        )
        formState.meta.fields["lastName"] = FieldMetaState(
            isDirty = true,
            isTouched = true
        )

        assertEquals(0, formState.resetKey)

        formState.reset(TestData())

        assertEquals(TestData(), formState.value)
        assertEquals(0, formState.meta.fields.size)
        assertEquals(1, formState.resetKey)
    }

    @Test
    fun testState_setError() {
        val initialValue = TestData(firstName = "John", lastName = "Doe")
        val formState = FormState(value = initialValue)

        // Add field metadata
        formState.meta.fields["firstName"] = FieldMetaState()
        formState.meta.fields["lastName"] = FieldMetaState()

        val firstNameError = FieldError("First name is required")
        val lastNameError = FieldError("Last name is too short")

        formState.setError(
            "firstName" to firstNameError,
            "lastName" to lastNameError
        )

        assertEquals(firstNameError, formState.meta.fields["firstName"]?.error)
        assertEquals(lastNameError, formState.meta.fields["lastName"]?.error)
    }

    @Test
    fun testState_saver() {
        val initialValue = TestData(firstName = "John", lastName = "Doe")
        val policy = FormPolicy()
        val formState = FormState(value = initialValue, policy = policy)

        formState.reset(initialValue)

        // Add some field metadata
        formState.meta.fields["firstName"] = FieldMetaState(
            error = FieldError("Some error"),
            isDirty = true
        )
        formState.meta.canSubmit = false

        val saver = FormState.Saver(autoSaver<TestData>(), policy)
        val scope = SaverScope { true }

        // Test save
        val saved = with(saver) { scope.save(formState) }
        assertTrue(saved is List<*>)

        // Test restore
        val restored = saver.restore(saved)
        assertTrue(restored is FormState<*>)

        assertEquals(formState.value, restored.value)
        assertEquals(formState.meta.canSubmit, restored.meta.canSubmit)
        assertEquals(formState.meta.fields.size, restored.meta.fields.size)
        assertEquals(formState.resetKey, restored.resetKey)

        val restoredFieldMeta = restored.meta.fields["firstName"]
        val originalFieldMeta = formState.meta.fields["firstName"]
        assertEquals(originalFieldMeta?.error, restoredFieldMeta?.error)
        assertEquals(originalFieldMeta?.isDirty, restoredFieldMeta?.isDirty)
    }

    @Test
    fun testMetaState() {
        val fields = mapOf(
            "firstName" to FieldMetaState(isDirty = true),
            "lastName" to FieldMetaState(isTouched = true)
        )
        val formMeta = FormMetaState(fields = fields, canSubmit = true)

        assertEquals(2, formMeta.fields.size)
        assertTrue(formMeta.canSubmit)
        assertTrue(formMeta.fields["firstName"]?.isDirty == true)
        assertTrue(formMeta.fields["lastName"]?.isTouched == true)
    }

    @Test
    fun testFormMetaState_saver() {
        val fields = mapOf(
            "firstName" to FieldMetaState(isDirty = true, error = FieldError("Error"))
        )
        val formMeta = FormMetaState(fields = fields, canSubmit = true)

        val saver = FormMetaState.Saver()
        val scope = SaverScope { true }

        // Test save
        val saved = with(saver) { scope.save(formMeta) }
        assertTrue(saved is List<*>)

        // Test restore
        val restored = saver.restore(saved)
        assertTrue(restored is FormMetaState)

        assertEquals(formMeta.canSubmit, restored.canSubmit)
        assertEquals(formMeta.fields.size, restored.fields.size)

        val restoredFieldMeta = restored.fields["firstName"]
        val originalFieldMeta = formMeta.fields["firstName"]
        assertEquals(originalFieldMeta?.isDirty, restoredFieldMeta?.isDirty)
        assertEquals(originalFieldMeta?.error, restoredFieldMeta?.error)
    }

    @Test
    fun testFieldMetaState() {
        val error = FieldError("Test error")
        val fieldMeta = FieldMetaState(
            error = error,
            mode = FieldValidationMode.Change,
            isDirty = true,
            isTouched = true,
            isValidated = true
        )

        assertEquals(error, fieldMeta.error)
        assertEquals(FieldValidationMode.Change, fieldMeta.mode)
        assertTrue(fieldMeta.isDirty)
        assertTrue(fieldMeta.isTouched)
        assertTrue(fieldMeta.isValidated)
    }

    @Test
    fun testFieldMetaState_saver() {
        val error = FieldError(listOf("Error 1", "Error 2"))
        val fieldMeta = FieldMetaState(
            error = error,
            mode = FieldValidationMode.Change,
            isDirty = true,
            isTouched = true,
            isValidated = true
        )

        val saver = FieldMetaState.Saver()
        val scope = SaverScope { true }

        // Test save
        val saved = with(saver) { scope.save(fieldMeta) }
        assertTrue(saved is List<*>)

        // Test restore
        val restored = saver.restore(saved)
        assertTrue(restored is FieldMetaState)

        assertEquals(fieldMeta.error, restored.error)
        assertEquals(fieldMeta.mode, restored.mode)
        assertEquals(fieldMeta.isDirty, restored.isDirty)
        assertEquals(fieldMeta.isTouched, restored.isTouched)
        assertEquals(fieldMeta.isValidated, restored.isValidated)
    }

    data class TestData(
        val firstName: String = "",
        val lastName: String = ""
    )
}
