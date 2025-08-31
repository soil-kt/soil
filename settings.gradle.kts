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
    id("com.gradle.develocity") version "4.1.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val version: String by extra.properties
develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"

        tag(if (System.getenv("CI").isNullOrBlank()) "Local" else "CI")
        tag(version)

        obfuscation {
            username { "Redacted" }
            hostname { "Redacted" }
            ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
        }

        // You can then add the --scan argument to any Gradle build to publish a Build Scan.
        // https://docs.gradle.com/develocity/gradle-plugin/current/
        publishing.onlyIf { false }
    }
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
