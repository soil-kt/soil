package soil.playground.query

import io.ktor.client.HttpClient
import soil.query.MutationReceiver
import soil.query.QueryReceiver

class KtorReceiver(
    val client: HttpClient
) : QueryReceiver, MutationReceiver
