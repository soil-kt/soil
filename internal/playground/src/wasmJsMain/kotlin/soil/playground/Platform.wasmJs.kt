package soil.playground

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

actual fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient {
        config(this)
    }
}
