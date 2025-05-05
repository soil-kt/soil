package soil.kmp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import soil.form.compose.Action
import soil.form.compose.Field
import soil.form.compose.Form
import soil.form.compose.rememberForm
import soil.form.compose.rememberFormState
import soil.form.compose.serializationSaver
import soil.form.rule.StringRuleBuilder
import soil.form.rule.StringRuleTester
import soil.form.rule.notBlank
import soil.form.rule.notNull
import soil.form.rules
import soil.playground.LocalFeedbackHost
import soil.playground.form.FormData
import soil.playground.form.compose.RadioGroup
import soil.playground.form.compose.SelectField
import soil.playground.form.compose.SubmitButton
import soil.playground.form.compose.InputField
import soil.playground.style.withAppTheme

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun HelloFormScreen() {
    val feedback = LocalFeedbackHost.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val formState = rememberFormState(initialValue = FormData(), saver = serializationSaver())
    val form = rememberForm(state = formState) {
        coroutineScope.launch {
            feedback.showAlert("Form submitted successfully")
            focusManager.clearFocus()
            formState.reset()
        }
    }
    HelloFormContent(
        form = form,
        modifier = Modifier.fillMaxSize()
    )
}

// The form input fields are based on the Live Demo used in React Hook Form.
// You can reference it here: https://react-hook-form.com/
@Composable
private fun HelloFormContent(
    form: Form<FormData>,
    modifier: Modifier = Modifier
) = withAppTheme {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val (f1, f2, f3, f4, f5, f6, f7) = FocusRequester.createRefs()
        form.FirstName(modifier = Modifier.fillMaxWidth().focusRequester(f1))
        form.LastName(modifier = Modifier.fillMaxWidth().focusRequester(f2))
        form.Email(modifier = Modifier.fillMaxWidth().focusRequester(f3))
        form.MobileNumber(modifier = Modifier.fillMaxWidth().focusRequester(f4))
        form.Title(modifier = Modifier.fillMaxWidth().focusRequester(f5))
        form.Developer(modifier = Modifier.fillMaxWidth().focusRequester(f6))
        form.Submit(modifier = Modifier.fillMaxWidth().focusRequester(f7))
    }
    // form.focusRequest
}

@Composable
private fun Form<FormData>.FirstName(
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Field(
        selector = { it.firstName },
        updater = { copy(firstName = it) },
        rules = rules {
            notBlank { "must be not blank" }
        }
    ) {
        it.InputField(
            modifier = modifier,
            label = { Text("First name") },
            singleLine = true,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
private fun Form<FormData>.LastName(
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Field(
        selector = { it.lastName },
        updater = { copy(lastName = it) },
        rules = rules {
            notBlank { "must be not blank" }
        }
    ) {
        it.InputField(
            modifier = modifier,
            label = { Text("Last name") },
            singleLine = true,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
private fun Form<FormData>.Email(
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Field(
        selector = { it.email },
        updater = { copy(email = it) },
        rules = rules {
            notBlank { "must be not blank" }
            email { "must be valid email address" }
        }
    ) {
        it.InputField(
            modifier = modifier,
            label = { Text("Email") },
            singleLine = true,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Email)
        )
    }
}

@Composable
private fun Form<FormData>.MobileNumber(
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Field(
        selector = { it.mobileNumber },
        updater = { copy(mobileNumber = it) },
        rules = rules {
            notBlank { "must be not blank" }
        }
    ) {
        it.InputField(
            modifier = modifier,
            label = { Text("Mobile number") },
            singleLine = true,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun Form<FormData>.Title(
    modifier: Modifier = Modifier
) {
    Field(
        selector = { it.title },
        updater = { copy(title = it) },
        rules = rules {
            notNull { "must be selected" }
        }
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        it.SelectField(
            expanded = isExpanded,
            onExpandedChange = { expanded -> isExpanded = expanded },
            modifier = modifier.onFocusEvent { state ->
                if (state.isFocused && it.value == null) {
                    isExpanded = true
                }
            },
            label = { Text("Title") },
        )
    }
}

@Composable
private fun Form<FormData>.Developer(
    modifier: Modifier = Modifier
) {
    Field(
        selector = { it.developer },
        updater = { copy(developer = it) },
        rules = rules {
            notNull { "must be selected" }
        }
    ) {
        it.RadioGroup(
            options = listOf(true, false),
            transform = { option -> if (option) "Yes" else "No" },
            modifier = modifier
        )
    }
}

@Composable
private fun Form<FormData>.Submit(
    modifier: Modifier = Modifier
) {
    Action {
        it.SubmitButton(
            modifier = modifier
        ) {
            Text(text = "Submit")
        }
    }
}

// Basic custom validation rule for email addresses
private fun StringRuleBuilder.email(message: () -> String) {
    val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    extend(StringRuleTester({ pattern.matches(this) }, message))
}
