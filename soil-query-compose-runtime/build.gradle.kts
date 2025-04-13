import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

val buildTarget = the<BuildTargetExtension>()

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        useEsModules()
        browser {
            // TODO: Add WASM tests
            //  Karma doesn't work
            //
            // > ./gradlew :soil-query-compose-runtime:wasmJsBrowserTest
            // Karma v6.4.4 server started at http://localhost:9876/
            // Launching browsers ChromeHeadless with concurrency unlimited
            // Starting browser ChromeHeadless
            // Connected on socket iqr25QfS_tnsIguBAAAB with id 19492594
            // Disconnected (0 times) reconnect failed before timeout of 2000ms (ping timeout)
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.soilQueryCore)
            api(projects.soilQueryCompose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(projects.internal.testing)
            api(projects.soilQueryTest)
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.compose.ui.test.junit4.android)
                implementation(libs.compose.ui.test.manifest)
            }
        }

        val skikoTest by creating {
            dependsOn(commonTest.get())
        }

        iosTest {
            dependsOn(skikoTest)
        }

        jvmTest {
            dependsOn(skikoTest)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        wasmJsTest {
            dependsOn(skikoTest)
        }
    }
}

android {
    namespace = "soil.query.compose.runtime"
    compileSdk = buildTarget.androidCompileSdk.get()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = buildTarget.androidMinSdk.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

composeCompiler {
    if (buildTarget.composeCompilerMetrics.getOrElse(false)) {
        metricsDestination = buildTarget.composeCompilerDestination
    }
    if (buildTarget.composeCompilerReports.getOrElse(false)) {
        reportsDestination = buildTarget.composeCompilerDestination
    }
}

kover {
    currentProject {
        createVariant("soil") {
            add("debug")
        }
    }
}
