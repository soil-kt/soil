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
            // TODO: Add WASM tests
            //  If you add `implementation(compose.ui)`, the tasks needed to run tests are automatically defined, but Karma doesn't work
            // - https://github.com/JetBrains/compose-multiplatform/blob/v1.7.3/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/web/internal/configureWebApplication.kt#L47
            // - https://github.com/JetBrains/compose-multiplatform/blob/v1.7.3/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/web/internal/configureWebApplication.kt#L82
            //
            // > ./gradlew :soil-query-compose:wasmJsBrowserTest
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
            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
            // TODO: CompositionLocal LocalLifecycleOwner not present in Android, it works only with Compose UI 1.7.0-alpha05 or above.
            //  Therefore, we will postpone adding this code until a future release.
            // implementation(libs.jbx.lifecycle.runtime.compose)
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
    namespace = "soil.query.compose"
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
