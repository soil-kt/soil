package soil.form


interface FieldTypeAdapter<V, S, U> {

    fun toValidationTarget(value: V): S

    fun toInput(value: V): U

    fun fromInput(value: U, current: V): V
}

class FieldPassthroughAdapter<V> : FieldTypeAdapter<V, V, V> {
    override fun toValidationTarget(value: V): V = value
    override fun toInput(value: V): V = value
    override fun fromInput(value: V, current: V): V = value
}
