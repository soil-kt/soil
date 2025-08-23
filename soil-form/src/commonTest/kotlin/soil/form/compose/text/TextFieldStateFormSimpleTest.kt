// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.text

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import soil.form.FieldValidator
import soil.form.compose.rememberForm
import soil.form.compose.ui.FieldLayout
import soil.form.compose.ui.InputField
import soil.form.compose.ui.Submit
import soil.form.rule.notEmpty
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TextFieldStateFormTest : UnitTest() {

    @Test
    fun testForm() = runComposeUiTest {
        var measured = false
        val textFieldState = TextFieldState()
        setContent {
            // ref: https://developer.android.com/codelabs/large-screens/keyboard-focus-management-in-compose#9
            LocalInputModeManager.current.requestInputMode(InputMode.Keyboard)
            val form = rememberForm(state = textFieldState.asFormState()) {
                // no-op
            }
            Scaffold(
                modifier = Modifier.fillMaxSize().onGloballyPositioned { measured = true },
                bottomBar = {
                    form.Submit()
                }
            ) {
                form.Field(
                    name = "testField",
                    validator = FieldValidator {
                        notEmpty { "Must be not empty" }
                    },
                    render = { field ->
                        FieldLayout(field) {
                            InputField()
                        }
                    }
                )
            }
        }
        waitUntil { measured }
        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("testField_error").assertDoesNotExist()
        onNodeWithTag("testField")
            .requestFocus()
            .performTextInput("")

        onNodeWithTag("testField")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("submit").assertIsFocused()

        waitUntilExactlyOneExists(hasTestTag("testField_error") and hasText("Must be not empty"))
        waitUntilExactlyOneExists(hasTestTag("submit") and isNotEnabled())

        onNodeWithTag("testField")
            .requestFocus()
            .performTextInput("hello")

        onNodeWithTag("testField")
            .performKeyInput { pressKey(Key.Tab) }

        onNodeWithTag("submit").assertIsFocused()

        waitUntilDoesNotExist(hasTestTag("testField_error"))
        waitUntilExactlyOneExists(hasTestTag("submit") and isEnabled())
    }
}
