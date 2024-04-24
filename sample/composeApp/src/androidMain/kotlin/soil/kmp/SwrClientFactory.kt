package soil.kmp

import soil.query.SwrClient

interface SwrClientFactory {
    val queryClient: SwrClient
}
