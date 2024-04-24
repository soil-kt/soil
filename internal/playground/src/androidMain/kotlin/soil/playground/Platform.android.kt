package soil.playground

import android.os.Parcelable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.parcelize.Parcelize

actual typealias CommonParcelable = Parcelable
actual typealias CommonParcelize = Parcelize

actual fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        config(this)
    }
}
