package soil.kmp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import soil.form.compose.Controller
import soil.form.compose.FieldControl
import soil.form.compose.Form
import soil.form.compose.FormScope
import soil.form.compose.rememberFieldRuleControl
import soil.form.compose.rememberSubmissionRuleAutoControl
import soil.form.compose.serializationSaver
import soil.form.rule.StringRuleBuilder
import soil.form.rule.StringRuleTester
import soil.form.rule.notBlank
import soil.form.rule.notNull
import soil.playground.LocalFeedbackHost
import soil.playground.form.FormData
import soil.playground.form.Title
import soil.playground.form.compose.FormRadioGroup
import soil.playground.form.compose.FormSelect
import soil.playground.form.compose.FormSubmit
import soil.playground.form.compose.FormTextField
import soil.playground.form.compose.rememberAsInputForEmail
import soil.playground.form.compose.rememberAsInputForNumber
import soil.playground.form.compose.rememberAsInputForText
import soil.playground.form.compose.rememberAsRadio
import soil.playground.form.compose.rememberAsSelect
import soil.playground.style.withAppTheme

@Composable
fun HelloFormScreen() {
    val feedback = LocalFeedbackHost.current
    val coroutineScope = rememberCoroutineScope()
    HelloFormContent(
        onSubmitted = {
            coroutineScope.launch { feedback.showAlert("Form submitted successfully") }
        },
        modifier = Modifier.fillMaxSize()
    )
}

// The form input fields are based on the Live Demo used in React Hook Form.
// You can reference it here: https://react-hook-form.com/
@OptIn(ExperimentalComposeUiApi::class, ExperimentalSerializationApi::class)
@Composable
private fun HelloFormContent(
    onSubmitted: (FormData) -> Unit,
    modifier: Modifier = Modifier
) = withAppTheme {
    var formVersion by rememberSaveable { mutableStateOf(0) }
    Form(
        onSubmit = { edited ->
            delay(3000) // dummy send data to server
            onSubmitted(edited)
            formVersion += 1
        },
        initialValue = FormData(),
        modifier = modifier,
        key = formVersion,
        saver = serializationSaver()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val (f1, f2, f3, f4, f5, f6, f7) = FocusRequester.createRefs()
            Controller(control = rememberFirstNameFieldControl()) { field ->
                val input = field.rememberAsInputForText(focusRequester = f1, focusNext = f2)
                FormTextField(binding = input)
            }

            Controller(control = rememberLastNameFieldControl()) { field ->
                val input = field.rememberAsInputForText(focusRequester = f2, focusNext = f3)
                FormTextField(binding = input)
            }

            Controller(control = rememberEmailFieldControl()) { field ->
                val input = field.rememberAsInputForEmail(focusRequester = f3, focusNext = f4)
                FormTextField(binding = input)
            }

            Controller(control = rememberMobileNumberFieldControl()) { field ->
                val input = field.rememberAsInputForNumber(focusRequester = f4, focusNext = f5)
                FormTextField(binding = input)
            }

            Controller(control = rememberTitleFieldControl()) { field ->
                val select = field.rememberAsSelect(focusRequester = f5, focusNext = f6)
                FormSelect(binding = select, displayText = Title::name)
            }

            Controller(control = rememberDeveloperFieldControl()) { field ->
                val radio = field.rememberAsRadio(focusRequester = f6, focusNext = f7)
                FormRadioGroup(binding = radio, options = listOf(true, false)) { if (it) "Yes" else "No" }
            }

            Controller(control = rememberSubmissionRuleAutoControl()) { submission ->
                FormSubmit(submission, label = "Submit", modifier = Modifier.focusRequester(f7))
            }
        }
    }
}

@Composable
private fun FormScope<FormData>.rememberFirstNameFieldControl(): FieldControl<String> {
    return rememberFieldRuleControl(
        name = "First name",
        select = { firstName },
        update = { copy(firstName = it) }
    ) {
        notBlank { "must be not blank" }
    }
}

@Composable
private fun FormScope<FormData>.rememberLastNameFieldControl(): FieldControl<String> {
    return rememberFieldRuleControl(
        name = "Last name",
        select = { lastName },
        update = { copy(lastName = it) }
    ) {
        notBlank { "must be not blank" }
    }
}

@Composable
private fun FormScope<FormData>.rememberEmailFieldControl(): FieldControl<String> {
    return rememberFieldRuleControl(
        name = "Email",
        select = { email },
        update = { copy(email = it) }
    ) {
        notBlank { "must be not blank" }
        email { "must be valid email address" }
    }
}

@Composable
private fun FormScope<FormData>.rememberMobileNumberFieldControl(): FieldControl<String> {
    return rememberFieldRuleControl(
        name = "Mobile number",
        select = { mobileNumber },
        update = { copy(mobileNumber = it) }
    ) {
        notBlank { "must be not blank" }
    }
}

@Composable
private fun FormScope<FormData>.rememberTitleFieldControl(): FieldControl<Title?> {
    return rememberFieldRuleControl(
        name = "Title",
        select = { title },
        update = { copy(title = it) }
    ) {
        notNull { "must be selected" }
    }
}

@Composable
private fun FormScope<FormData>.rememberDeveloperFieldControl(): FieldControl<Boolean?> {
    return rememberFieldRuleControl(
        name = "Developer",
        select = { developer },
        update = { copy(developer = it) }
    ) {
        notNull { "must be selected" }
    }
}

// Basic custom validation rule for email addresses
private fun StringRuleBuilder.email(message: () -> String) {
    val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    extend(StringRuleTester({ pattern.matches(this) }, message))
}
