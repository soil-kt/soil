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
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.jbx.savedstate)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(projects.internal.testing)
        }

        val skikoMain by creating {
            dependsOn(commonMain.get())
        }

        val skikoTest by creating {
            dependsOn(commonTest.get())
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.compose.ui.test.junit4.android)
                implementation(libs.compose.ui.test.manifest)
            }
        }

        iosMain {
            dependsOn(skikoMain)
        }

        iosTest {
            dependsOn(skikoTest)
        }

        jvmMain {
            dependsOn(skikoMain)
        }

        jvmTest {
            dependsOn(skikoTest)
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        wasmJsMain {
            dependsOn(skikoMain)
        }

        wasmJsTest {
            dependsOn(skikoTest)
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
