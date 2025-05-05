package soil.form


interface FieldTypeAdapter<V, S, U> {

    fun toValidationTarget(value: V): S

    fun toRawInput(value: V): U

    fun fromRawInput(value: U, current: V): V
}

class FieldPassthroughAdapter<V> : FieldTypeAdapter<V, V, V> {
    override fun toValidationTarget(value: V): V = value
    override fun toRawInput(value: V): V = value
    override fun fromRawInput(value: V, current: V): V = value
}
