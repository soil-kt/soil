import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

val buildTarget = the<BuildTargetExtension>()

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = buildTarget.javaVersion.get().toString()
            }
        }
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.kotlinx.coroutines.core)
        }

        val skikoMain by creating {
            dependsOn(commonMain.get())
        }

        iosMain {
            dependsOn(skikoMain)
        }

        jvmMain {
            dependsOn(skikoMain)
        }

        named("wasmJsMain") {
            dependsOn(skikoMain)
        }
    }
}

android {
    namespace = "soil.form"
    compileSdk = buildTarget.androidCompileSdk.get()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = buildTarget.androidMinSdk.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = buildTarget.javaVersion.get()
        targetCompatibility = buildTarget.javaVersion.get()
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}
