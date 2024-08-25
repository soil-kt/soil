import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
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
        composeCompilerDestination = layout.buildDirectory.dir("compose-compiler")
        composeCompilerMetrics = providers.gradleProperty("composeCompilerMetrics").map { it.toBoolean() }
        composeCompilerReports = providers.gradleProperty("composeCompilerReports").map { it.toBoolean() }
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // Note: Kotlin 2.0.20 ~
            // https://kotlinlang.org/docs/whatsnew2020.html#data-class-copy-function-to-have-the-same-visibility-as-constructor
            freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        }
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
