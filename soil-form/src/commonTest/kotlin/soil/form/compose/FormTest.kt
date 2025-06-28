// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import soil.form.FieldTypeAdapter
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.compose.ui.InputField
import soil.form.compose.ui.Submit
import soil.form.noFieldError
import soil.form.rule.notEmpty
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class FormTest : UnitTest() {

    @Test
    fun testForm() = runComposeUiTest {
        val formState = FormState(value = TestData())
        var submittedFormData: TestData? = null
        setContent {
            val form = rememberForm(state = formState) {
                submittedFormData = it
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    form.Submit()
                }
            ) {
                Column {
                    form.FirstName { InputField(ref = it) }
                    form.LastName { InputField(ref = it) }
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("lastName")
            .requestFocus()
            .performTextInput("Bar")

        onNodeWithTag("submit")
            .requestFocus()

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("submit").performClick()

        waitUntil { submittedFormData == formState.value }

        assertEquals(formState.value.firstName, "Foo")
        assertEquals(formState.value.lastName, "Bar")
    }

    @Test
    fun testForm_withMinimalPolicy() = runComposeUiTest {
        val formState = FormState(value = TestData(), policy = FormPolicy.Minimal)
        var submittedFormData: TestData? = null
        setContent {
            val form = rememberForm(state = formState) {
                submittedFormData = it
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    form.Submit()
                }
            ) {
                Column {
                    form.FirstName { InputField(ref = it) }
                    form.LastName { InputField(ref = it) }
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("lastName")
            .requestFocus()
            .performTextInput("")

        onNodeWithTag("submit")
            .requestFocus()

        onNodeWithTag("submit").performClick()

        assertNull(submittedFormData)

        val fieldMeta = checkNotNull(formState.meta.fields["lastName"])
        assertEquals(FieldValidationMode.Change, fieldMeta.mode)
        assertNotEquals(noFieldError, fieldMeta.error)
    }

    @Test
    fun testForm_withFieldValidationError() = runComposeUiTest {
        val formState = FormState(value = TestData())
        setContent {
            val form = rememberForm(state = formState) {
                // no-op
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    form.Submit()
                }
            ) {
                Column {
                    form.FirstName { InputField(ref = it) }
                    form.LastName { InputField(ref = it) }
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("lastName")
            .requestFocus()
            .performTextInput("Bar")

        onNodeWithTag("submit")
            .requestFocus()

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("lastName")
            .requestFocus()
            .performTextClearance()

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())
    }

    @Test
    fun testForm_withFieldValueStateOnly() = runComposeUiTest {
        val textFieldState = TextFieldState("")
        val formState = FormState(value = textFieldState, FormMetaState())
        var submittedFormData: CharSequence? = null
        setContent {
            val form = rememberForm(state = formState) {
                submittedFormData = it.text
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    form.Submit()
                }
            ) {
                Column {
                    form.Field(
                        selector = { it },
                        updater = { it },
                        adapter = TextFieldStateAdapter,
                        render = { field ->
                            BasicTextField(
                                state = field.value,
                                modifier = Modifier
                                    .onFocusChanged { state ->
                                        field.handleFocus(state.isFocused || state.hasFocus)
                                    }
                                    .testTag("textFieldOnly"),
                                enabled = field.isEnabled
                            )
                        }
                    )
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 1 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("textFieldOnly")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("submit")
            .requestFocus()

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("submit").performClick()

        waitUntil { submittedFormData == formState.value.text }
    }

    data class TestData(
        val firstName: String = "",
        val lastName: String = ""
    )

    @Composable
    fun Form<TestData>.FirstName(
        enabled: Boolean = true,
        content: @Composable (FormField<String>) -> Unit
    ) {
        Field(
            selector = { it.firstName },
            updater = { copy(firstName = it) },
            validator = FieldValidator {
                notEmpty { "Must be not empty" }
            },
            name = "firstName",
            enabled = enabled,
            render = content
        )
    }

    @Composable
    fun Form<TestData>.LastName(
        enabled: Boolean = true,
        content: @Composable (FormField<String>) -> Unit,
    ) {
        Field(
            selector = { it.lastName },
            updater = { copy(lastName = it) },
            validator = FieldValidator {
                notEmpty { "Must be not empty" }
            },
            name = "lastName",
            enabled = enabled,
            render = content
        )
    }

    object TextFieldStateAdapter : FieldTypeAdapter<TextFieldState, String, TextFieldState> {
        override fun toValidationTarget(value: TextFieldState): String = value.text.toString()
        override fun toInput(value: TextFieldState): TextFieldState = value
        override fun fromInput(value: TextFieldState, current: TextFieldState): TextFieldState = current
    }
}
