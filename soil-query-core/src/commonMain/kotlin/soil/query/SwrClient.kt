// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

interface SwrClient : MutationClient, QueryClient {

    fun perform(sideEffects: QueryEffect)

    fun onMount(id: String)

    fun onUnmount(id: String)
}
