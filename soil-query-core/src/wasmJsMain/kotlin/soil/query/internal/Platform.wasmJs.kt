// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

actual fun epoch(): Long {
    return Date.now().toString().toLong() / 1000
}

actual fun uuid(): String {
    return crypto.randomUUID()
}

// NOTE: This API can only be used with https, except on localhost.
external interface Crypto {
    fun randomUUID(): String
}

external val crypto: Crypto

external object Date {
    fun now(): JsNumber
}

// FIXME: visibilityState does not exist in the Document API definitions within the kotlinx.browser package.
external interface Document {
    val visibilityState: String
}

external val document: Document
