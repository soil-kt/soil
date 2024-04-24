// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

interface MutationClient {

    val defaultMutationOptions: MutationOptions

    fun <T, S> getMutation(
        key: MutationKey<T, S>
    ): MutationRef<T, S>
}
