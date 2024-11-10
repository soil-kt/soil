![Soil](art/Logo.svg)

[![Release](https://img.shields.io/maven-central/v/com.soil-kt.soil/query-core?style=for-the-badge&color=62CC6A)](https://github.com/soil-kt/soil)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)

# Compose-First Power Packs

Simplify Compose, Accelerate Development :rocket:

- **Query** -
  A seamless data fetching and caching. written more declaratively, leading to more readable code.
- **Form** -
  A extensible validation control and form state management. minimizes the impact of re-composition.
- **Space** -
  A flexible scoped state management. collaborating with the navigation library to create new scopes.


## Try It Online

The Soil library for Kotlin Multiplatform now includes experimental support for [Kotlin Wasm](https://kotlinlang.org/docs/wasm-overview.html).
If your browser supports [WasmGC](https://github.com/WebAssembly/gc), you can run the sample app directly in the browser.


:point_right: [Sample App](https://play.soil-kt.com/)

Source code: <https://github.com/soil-kt/soil/tree/main/sample/>

> [!NOTE]
> Currently, the only browsers that support WasmGC are Chrome and Firefox. For the latest compatibility information, please visit https://webassembly.org/features/.


## Download

Soil is available on `mavenCentral()`.

```kts
dependencies {
    val soil = "1.0.0-alpha07"

    // Query
    implementation("com.soil-kt.soil:query-core:$soil")
    // Query utilities for Compose
    implementation("com.soil-kt.soil:query-compose:$soil")
    // optional - helpers for Compose
    implementation("com.soil-kt.soil:query-compose-runtime:$soil")
    // optional - receivers for Ktor (3.x)
    implementation("com.soil-kt.soil:query-receivers-ktor:$soil")
    // optional - Test helpers
    testImplementation("com.soil-kt.soil:query-test:$soil")

    // Form
    implementation("com.soil-kt.soil:form:$soil")

    // Space
    implementation("com.soil-kt.soil:space:$soil")
}
```

## [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) compatibility

Supported targets:

- Android
- iOS
- Desktop (JVM)
- Web (Wasm)


## Documentation

Please visit [docs.soil-kt.com](https://docs.soil-kt.com/) for Quick Start, guides of features and more.

* Getting started with [Query](https://docs.soil-kt.com/guide/query/hello-query)
* Getting started with [Form](https://docs.soil-kt.com/guide/form/hello-form)
* Getting started with [Space](https://docs.soil-kt.com/guide/space/hello-space)


## Special Thanks

Thank you for featuring our library in the following sources:

- [jetc.dev Newsletter Issue #212](https://jetc.dev/issues/212.html)
- [Android Dev Notes #Twitter](https://twitter.com/androiddevnotes/status/1792409220484350109)
- [Android Weekly Issue #624](https://androidweekly.net/issues/issue-624)


## License

```
Copyright 2024 Soil Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
