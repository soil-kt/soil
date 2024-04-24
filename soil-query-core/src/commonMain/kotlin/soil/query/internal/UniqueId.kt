// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

interface UniqueId {
    val namespace: String
    val tags: Array<out SurrogateKey>
}

typealias SurrogateKey = Any
