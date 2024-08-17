plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

allprojects {
    buildTarget {
        androidCompileSdk = providers.gradleProperty("androidCompileSdk").map { it.toInt() }
        androidMinSdk = providers.gradleProperty("androidMinSdk").map { it.toInt() }
        androidTargetSdk = providers.gradleProperty("androidTargetSdk").map { it.toInt() }
        javaVersion = provider { JavaVersion.VERSION_11 }
    }

    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        format("format") {
            target("src/**/*.kt", "*.gradle.kts")
            targetExclude("$projectDir/build/**/*.kt")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        format("formatYaml") {
            target(".github/**/*.yml")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        if (project.path in publicModules) {
            kotlin {
                // ref. https://github.com/diffplug/spotless/tree/main/plugin-gradle#how-can-i-enforce-formatting-gradually-aka-ratchet
                // ratchetFrom = "origin/main"
                target("**/*.kt")
                targetExclude("$projectDir/build/**/*.kt")

                licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
            }
        }
    }
}
