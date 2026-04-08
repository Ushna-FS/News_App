plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "com.example.shared"
        compileSdk {
            version = release(36) {
                minorApiLevel = 1
            }
        }
        minSdk = 24
        androidResources.enable = true

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
                // Ktor core
                implementation(libs.ktor.client.core)

                // serialization
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)

                // logging
                implementation(libs.ktor.client.logging)

                // json
                implementation(libs.kotlinx.serialization.json)

                // Koin Core (Multiplatform)
                implementation(libs.koin.core)
                //koin VM

                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // Material Icons
                implementation(libs.material.icons.extended)

                //firebase
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.gitlive.firebase.crashlytics)

                //paging
                implementation(libs.paging.compose.common)
                implementation(libs.paging.common)

                //room
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)

                implementation(libs.kotlinx.datetime)

                // Compose Multiplatform
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.foundation)

                // Compose lifecycle
                implementation(libs.lifecycle.viewmodel.compose)

                // Compose navigation (multiplatform)
                implementation(libs.navigation.compose)

                // Compose ViewModel
                implementation(libs.lifecycle.viewmodel)

                // Compose coroutine support
                implementation(libs.kotlinx.coroutines.core)
                //preview
                implementation(libs.jetbrains.ui.tooling.preview)
                implementation(libs.lifecycle.runtime.compose)

                //coil
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.ktor.client.okhttp)
                // Android specific Koin features
                implementation(libs.koin.android)
                //firebase
                implementation(project.dependencies.platform(libs.firebase.bom))

                //room
                implementation(libs.androidx.room.runtime)
                //worker
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.koin.androidx.workmanager)

                // json
                implementation(libs.kotlinx.serialization.json)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit.v115)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.foundation)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.

                implementation(libs.ktor.client.darwin)
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}
compose.resources {
    publicResClass = true
    packageOfResClass = "me.sample.library.resources"
    generateResClass = auto
}
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    androidRuntimeClasspath(libs.jetbrains.ui.tooling)
}