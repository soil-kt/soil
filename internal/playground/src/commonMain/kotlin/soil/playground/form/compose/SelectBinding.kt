package soil.playground.form.compose

//@Stable
//class SelectBinding<T>(
//    private val rawField: Field<T>,
//    private val rawModifier: Modifier,
//    val onSelect: (T) -> Unit,
//    val expanded: Boolean,
//    val onExpandedChange: (Boolean) -> Unit,
//    val onDismissRequest: () -> Unit = { onExpandedChange(false) }
//) {
//    val name: String get() = rawField.name
//    val value: T get() = rawField.value
//    val hasError: Boolean get() = rawField.hasError
//    val errors: FieldErrors get() = rawField.errors
//    val enabled: Boolean get() = rawField.isEnabled
//    val modifier: Modifier get() = rawModifier
//}
//
//@Composable
//fun <T> Field<T>.rememberAsSelect(
//    focusRequester: FocusRequester? = null,
//    focusNext: FocusRequester? = null,
//    focusNextKeyEvent: (KeyEvent) -> Boolean = { false }
//): SelectBinding<T> {
//    var expanded by remember(this) { mutableStateOf(false) }
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
//
//    val modifier = remember(focusRequester, focusNext, handlePreviewKeyEvent) {
//        Modifier
//            .onFocusChanged {
//                expanded = isEnabled && it.isFocused
//            }
//            .onFocusChanged(this)
//            .onPreviewKeyEvent(handlePreviewKeyEvent)
//            .ifNotNull(focusRequester) {
//                focusRequester(it)
//            }
//            .ifNotNull(focusNext) {
//                focusProperties { next = it }
//            }
//    }
//
//    val onSelect = remember<(T) -> Unit>(this) {
//        { value ->
//            onChange(value)
//            expanded = false
//            handleFocusNext()
//        }
//    }
//
//    return SelectBinding(
//        rawField = this,
//        rawModifier = modifier,
//        onSelect = onSelect,
//        expanded = expanded,
//        onExpandedChange = { expanded = !it }
//    )
//}
