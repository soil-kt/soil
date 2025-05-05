// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.CompletableDeferred
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.ValidationRuleSet
import soil.form.rule.notEmpty
import soil.form.rules
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FormTest : UnitTest() {

    @Test
    fun testFormState() = runComposeUiTest {
        var submitCalled = false
        val mockSubmit = CompletableDeferred<Unit>()
        setContent {
            var submitCount by remember { mutableIntStateOf(0) }
            val form = rememberForm(initialValue = TestData(), saver = TestData.saver()) {
                println("XXX/Debug: Submit=$it")
                submitCalled = true
                submitCount++
            }
            SideEffect {
                println("XXX/Debug: SideEffect=$form")
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    form.Action {
                        Button(
                            onClick = it::submit,
                            enabled = it.canSubmit,
                            modifier = Modifier.testTag("submit")
                        ) {
                            Text("Submit")
                        }
                    }
                }
            ) {
                Column {
                    form.FirstNameField(name = "firstName") {
                        BasicTextField(
                            value = it.value,
                            onValueChange = it::onValueChange,
                            modifier = Modifier.onFocusChanged(it)
                                .testTag("firstName")

                        )
                    }

                    form.LastNameField(name = "lastName") {
                        BasicTextField(
                            value = it.value,
                            onValueChange = it::onValueChange,
                            modifier = Modifier.onFocusChanged(it)
                                .testTag("lastName")
                        )
                    }
                }

                Text(submitCount.toString(), modifier = Modifier.testTag("count"))
            }
        }

        onNodeWithTag("submit").assertHasClickAction().assertIsEnabled()
        onNodeWithTag("submit").performClick()
        waitUntil { submitCalled }
        onNodeWithTag("submit").assertIsNotEnabled()
        mockSubmit.complete(Unit)
        waitForIdle()
        waitUntilExactlyOneExists(hasTestTag("count") and hasText("1"))
    }

    data class TestData(
        val firstName: String = "",
        override val lastName: String = "",
        val age: Int = 0
    ) : LastNameField {
        companion object {
            fun saver(): Saver<TestData, Any> = mapSaver(
                save = { value ->
                    mapOf(
                        "firstName" to value.firstName,
                        "lastName" to value.lastName,
                        "age" to value.age
                    )
                },
                restore = { map ->
                    TestData(
                        firstName = map["firstName"] as String,
                        lastName = map["lastName"] as String,
                        age = map["age"] as Int
                    )
                }
            )
        }
    }

    interface LastNameField {
        val lastName: String
    }

    @Composable
    fun Form<TestData>.FirstNameField(
        name: FieldName? = null,
        dependsOn: FieldNames? = null,
        disabled: Boolean = false,
        content: @Composable (FormFieldControl<String>) -> Unit
    ) {
        Field(
            selector = { it.firstName },
            updater = { copy(firstName = it) },
            rules = rememberNameFieldRules(),
            name = name,
            dependsOn = dependsOn,
            disabled = disabled,
            content = content
        )
    }

    @Composable
    fun Form<TestData>.LastNameField(
        name: FieldName? = null,
        dependsOn: FieldNames? = null,
        disabled: Boolean = false,
        content: @Composable (FormFieldControl<String>) -> Unit
    ) {
        LastNameField(
            updater = { copy(lastName = it) },
            name = name,
            dependsOn = dependsOn,
            disabled = disabled,
            content = content
        )
    }

    @Composable
    fun <T> Form<T>.LastNameField(
        updater: T.(String) -> T,
        name: FieldName? = null,
        dependsOn: FieldNames? = null,
        disabled: Boolean = false,
        content: @Composable (FormFieldControl<String>) -> Unit
    ) where T : LastNameField {
        Field(
            selector = { it.lastName },
            updater = updater,
            rules = rememberNameFieldRules(),
            name = name,
            dependsOn = dependsOn,
            disabled = disabled,
            content = content
        )
    }

    @Composable
    fun rememberNameFieldRules(): ValidationRuleSet<String> {
        return remember {
            rules {
                notEmpty { "Must be not empty" }
            }
        }
    }
}
