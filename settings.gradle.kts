pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
//        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental") {
//            content {
//                includeGroupByRegex("io\\.ktor.*")
//                includeGroupByRegex("org\\.jetbrains.*")
//            }
//        }
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
//        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "soil"

// https://docs.gradle.org/7.4/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Public modules
include(
    ":soil-experimental:soil-lazyload",
    ":soil-experimental:soil-optimistic-update",
    ":soil-experimental:soil-reacty",
    ":soil-query-core",
    ":soil-query-compose",
    ":soil-query-receivers:ktor",
    ":soil-query-test",
    ":soil-form",
    ":soil-serialization-bundle",
    ":soil-space"
)

// Private modules
include(
    ":sample:composeApp",
    ":internal:playground",
    ":internal:testing"
)
