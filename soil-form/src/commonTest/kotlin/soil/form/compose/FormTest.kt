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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.text.input.VisualTransformation
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldValidator
import soil.form.rule.notEmpty
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FormTest : UnitTest() {

    @Test
    fun testForm_submit() = runComposeUiTest {
        val formState = formStateOf(value = TestData())
        var submittedFormData: TestData? = null
        setContent {
            val form = rememberForm(state = formState) {
                submittedFormData = it
                println("XXX/Debug: Submit=$it")
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

        onNodeWithTag("submit").assertIsNotEnabled()

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
    }

    data class TestData(
        val firstName: String = "",
        val lastName: String = ""
    ) {
        companion object {
            val Saver = listSaver(
                save = { value ->
                    listOf(
                        value.firstName,
                        value.lastName
                    )
                },
                restore = { list ->
                    val (firstName, lastName) = list
                    TestData(
                        firstName = firstName as String,
                        lastName = lastName as String
                    )
                }
            )
        }
    }

    @Composable
    fun Form<*>.Submit(
        modifier: Modifier = Modifier
    ) {
        Action {
            Button(
                onClick = it::submit,
                enabled = it.canSubmit,
                modifier = modifier.focusable()
            ) {
                Text("Submit")
            }
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
