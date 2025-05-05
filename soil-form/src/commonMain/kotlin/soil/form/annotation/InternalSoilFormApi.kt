package soil.form.annotation

@RequiresOptIn(message = "This API is internal to Soil Form and should not be used from outside the library.")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CONSTRUCTOR
)
annotation class InternalSoilFormApi
