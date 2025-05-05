package soil.form.compose

import androidx.compose.runtime.Composable

@Composable
fun <T : Any> Form<T>.Action(
    content: @Composable (FormActionControl<T>) -> Unit
) {
    val control = rememberFormActionControl()
    content(control)
}
