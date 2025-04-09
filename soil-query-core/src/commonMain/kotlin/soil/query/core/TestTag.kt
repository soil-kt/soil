// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Interface for tagging queries, mutations, and subscriptions to enable mocking during testing and previews.
 * 
 * TestTag provides an alternative to ID-based mocking, which is particularly useful when dealing with 
 * automatically generated IDs that may change during configuration changes or recompositions.
 * 
 * Usage example:
 * ```kotlin
 * // Define a test tag
 * class MyQueryTestTag : QueryTestTag<String>("my-query")
 * 
 * // Use the test tag with a mocking client
 * testClient.on(MyQueryTestTag()) { "mocked data" }
 * 
 * // Apply the test tag when getting a query
 * val query = client.getQuery(queryKey, Marker.testTag(MyQueryTestTag()))
 * ```
 * 
 * This approach allows for consistent mocking even when queries use auto-generated IDs,
 * ensuring reliable test and preview behavior in UI components.
 */
interface TestTag {
    val tag: String
}
