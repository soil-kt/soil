package soil.playground.form.compose

import androidx.compose.ui.Modifier

internal inline fun <T : Any> Modifier.ifNotNull(
    value: T?,
    builder: Modifier.(T) -> Modifier
): Modifier {
    return if (value != null) {
        then(builder(value))
    } else {
        this
    }
}

internal inline fun Modifier.ifTrue(
    value: Boolean, builder: Modifier.() -> Modifier
): Modifier {
    return if (value) then(builder()) else this
}
