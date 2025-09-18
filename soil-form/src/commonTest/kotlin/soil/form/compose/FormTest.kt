// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import soil.form.FieldOptions
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.FormOptions
import soil.form.compose.ui.InputField
import soil.form.compose.ui.Submit
import soil.form.compose.ui.WithLayout
import soil.form.noFieldError
import soil.form.rule.notEmpty
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration

@OptIn(ExperimentalTestApi::class)
class FormTest : UnitTest() {

    private val testPolicy = FormPolicy(
        formOptions = FormOptions(
            preValidationDelayOnMount = Duration.ZERO,
            preValidationDelayOnChange = Duration.ZERO
        ),
        fieldOptions = FieldOptions(
            validationDelayOnMount = Duration.ZERO,
            validationDelayOnBlur = Duration.ZERO,
            validationDelayOnChange = Duration.ZERO
        )
    )

    @Test
    fun testForm() = runComposeUiTest {
        val formState = FormState(
            value = TestData(),
            policy = testPolicy
        )
        var submittedFormData: TestData? = null
        setContent {
            // ref: https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose#9
            LocalInputModeManager.current.requestInputMode(InputMode.Keyboard)
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
                    form.FirstName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                    form.LastName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("firstName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("lastName")
            .performTextInput("Bar")

        onNodeWithTag("lastName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("submit").assertIsFocused()

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
            // ref: https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose#9
            LocalInputModeManager.current.requestInputMode(InputMode.Keyboard)
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
                    form.FirstName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                    form.LastName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                }
            }
        }
        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("submit").performClick()

        assertNull(submittedFormData)

        val fieldMeta = checkNotNull(formState.meta.fields["lastName"])
        assertEquals(FieldValidationMode.Change, fieldMeta.mode)
        assertNotEquals(noFieldError, fieldMeta.error)

        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("Foo")

        onNodeWithTag("firstName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("lastName")
            .performTextInput("Bar")

        onNodeWithTag("lastName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("submit").assertIsFocused()

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("submit").performClick()

        waitUntil { submittedFormData == formState.value }

        assertEquals(formState.value.firstName, "Foo")
        assertEquals(formState.value.lastName, "Bar")
    }

    @Test
    fun testForm_withInitialValidValues() = runComposeUiTest {
        val initialData = TestData(
            firstName = "foo",
            lastName = "bar"
        )
        val formState = FormState(
            value = initialData,
            policy = testPolicy
        )
        var submittedFormData: TestData? = null
        setContent {
            // ref: https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose#9
            LocalInputModeManager.current.requestInputMode(InputMode.Keyboard)
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
                    form.FirstName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                    form.LastName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                }
            }
        }

        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())

        onNodeWithTag("submit").performClick()

        waitUntil { submittedFormData == formState.value }

        assertEquals(initialData.firstName, submittedFormData?.firstName)
        assertEquals(initialData.lastName, submittedFormData?.lastName)
    }

    @Test
    fun testForm_withFieldValidationError() = runComposeUiTest {
        val formState = FormState(value = TestData(), policy = testPolicy)
        setContent {
            // ref: https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose#9
            LocalInputModeManager.current.requestInputMode(InputMode.Keyboard)
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
                    form.FirstName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                    form.LastName { field ->
                        field.WithLayout {
                            InputField()
                        }
                    }
                }
            }
        }

        waitUntil { formState.meta.fields.count() == 2 }

        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("firstName_error").assertDoesNotExist()
        onNodeWithTag("firstName")
            .requestFocus()
            .performTextInput("")

        onNodeWithTag("firstName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("lastName_error").assertDoesNotExist()

        onNodeWithTag("lastName")
            .performTextInput("")

        onNodeWithTag("lastName")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("submit").assertIsFocused()

        waitUntilExactlyOneExists(hasTestTag("firstName_error") and hasText("Must be not empty"))
        waitUntilExactlyOneExists(hasTestTag("lastName_error") and hasText("Must be not empty"))
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
}
