package soil.playground

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
expect annotation class CommonParcelize()

expect interface CommonParcelable

expect fun createHttpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient
