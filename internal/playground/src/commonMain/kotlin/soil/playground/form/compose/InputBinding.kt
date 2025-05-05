package soil.playground.form.compose

//@Stable
//class InputBinding(
//    private val rawField: Field<String>,
//    private val rawModifier: Modifier,
//    val keyboardOptions: KeyboardOptions,
//    val visualTransformation: VisualTransformation,
//    val singleLine: Boolean
//) {
//    val name: String get() = rawField.name
//    val value: String get() = rawField.value
//    val onValueChange: (String) -> Unit get() = rawField.onChange
//    val hasError: Boolean get() = rawField.hasError
//    val errors: FieldErrors get() = rawField.errors
//    val enabled: Boolean get() = rawField.isEnabled
//    val modifier: Modifier get() = rawModifier
//}
//
//@Composable
//fun Field<String>.rememberAsInputForText(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Text,
//        imeAction = ImeAction.Next
//    ),
//    singleLine: Boolean = true,
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    val focusManager = LocalFocusManager.current
//    val handleFocusNext = remember(focusNext) {
//        { focusNext?.requestFocus() ?: focusManager.moveFocus(FocusDirection.Next) }
//    }
//    val handlePreviewKeyEvent = remember<(KeyEvent) -> Boolean>(handleFocusNext, focusNextKeyEvent) {
//        { event ->
//            if (focusNextKeyEvent(event)) {
//                handleFocusNext()
//                true
//            } else {
//                false
//            }
//        }
//    }
//    val modifier = remember(focusRequester, focusNext, handlePreviewKeyEvent) {
//        Modifier
//            .onFocusChanged(this)
//            .onPreviewKeyEvent(handlePreviewKeyEvent)
//            .ifNotNull(focusRequester) {
//                focusRequester(it)
//            }
//            .ifNotNull(focusNext) {
//                focusProperties { next = it }
//            }
//    }
//    return InputBinding(
//        rawField = this,
//        rawModifier = modifier,
//        keyboardOptions = keyboardOptions,
//        singleLine = singleLine,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForTextArea(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Text,
//        imeAction = ImeAction.Default
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = false,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForPassword(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Password,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = PasswordVisualTransformation()
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForNumber(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Number,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForEmail(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Email,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForPhone(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Phone,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForUri(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Uri,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
//
//@Composable
//fun Field<String>.rememberAsInputForDecimal(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false },
//    keyboardOptions: KeyboardOptions = KeyboardOptions(
//        keyboardType = KeyboardType.Decimal,
//        imeAction = ImeAction.Next
//    ),
//    visualTransformation: VisualTransformation = VisualTransformation.None
//): InputBinding {
//    return rememberAsInputForText(
//        focusRequester = focusRequester,
//        focusNext = focusNext,
//        focusNextKeyEvent = focusNextKeyEvent,
//        keyboardOptions = keyboardOptions,
//        singleLine = true,
//        visualTransformation = visualTransformation
//    )
//}
