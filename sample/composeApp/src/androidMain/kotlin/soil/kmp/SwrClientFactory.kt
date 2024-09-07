package soil.kmp

import soil.query.SwrClientPlus

interface SwrClientFactory {
    val queryClient: SwrClientPlus
}
