// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
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
import androidx.compose.ui.text.input.VisualTransformation
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldValidationMode
import soil.form.FieldValidator
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
                    form.Submit(modifier = Modifier.testTag("submit"))
                }
            ) {
                Column {
                    form.FirstName(modifier = Modifier.testTag("firstName"))
                    form.LastName(modifier = Modifier.testTag("lastName"))
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
                    form.Submit(modifier = Modifier.testTag("submit"))
                }
            ) {
                Column {
                    form.FirstName(modifier = Modifier.testTag("firstName"), name = "firstName")
                    form.LastName(modifier = Modifier.testTag("lastName"), name = "lastName")
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
                    form.Submit(modifier = Modifier.testTag("submit"))
                }
            ) {
                Column {
                    form.FirstName(modifier = Modifier.testTag("firstName"), name = "firstName")
                    form.LastName(modifier = Modifier.testTag("lastName"), name = "lastName")
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

    data class TestData(
        val firstName: String = "",
        val lastName: String = ""
    )

    @Composable
    fun Form<*>.Submit(
        modifier: Modifier = Modifier
    ) {
        Button(
            onClick = ::handleSubmit,
            enabled = state.meta.canSubmit,
            modifier = modifier.focusable()
        ) {
            Text("Submit")
        }
    }

    @Composable
    fun Form<TestData>.FirstName(
        modifier: Modifier = Modifier,
        name: FieldName? = null,
        dependsOn: FieldNames? = null,
        enabled: Boolean = true
    ) {
        Field(
            selector = { it.firstName },
            updater = { copy(firstName = it) },
            validator = FieldValidator {
                notEmpty { "Must be not empty" }
            },
            name = name,
            dependsOn = dependsOn,
            enabled = enabled
        ) {
            it.TextField(
                modifier = modifier,
                singleLine = true
            )
        }
    }

    @Composable
    fun Form<TestData>.LastName(
        modifier: Modifier = Modifier,
        name: FieldName? = null,
        dependsOn: FieldNames? = null,
        enabled: Boolean = true
    ) {
        Field(
            selector = { it.lastName },
            updater = { copy(lastName = it) },
            validator = FieldValidator {
                notEmpty { "Must be not empty" }
            },
            name = name,
            dependsOn = dependsOn,
            enabled = enabled
        ) {
            it.TextField(
                modifier = modifier,
                singleLine = true
            )
        }
    }

    @Composable
    fun FormFieldControl<String>.TextField(
        modifier: Modifier = Modifier,
        value: String = this.value,
        onValueChange: (String) -> Unit = this::onValueChange,
        keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
        keyboardActions: KeyboardActions = KeyboardActions.Default,
        singleLine: Boolean = false,
        maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
        minLines: Int = 1,
        visualTransformation: VisualTransformation = VisualTransformation.None,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.onFocusChanged { state -> handleFocus(state.isFocused || state.hasFocus) },
            enabled = isEnabled,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            isError = hasError,
        )
    }
}
