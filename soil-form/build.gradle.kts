import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
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
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

kover {
    currentProject {
        createVariant("soil") {
            add("debug")
        }
    }
}
