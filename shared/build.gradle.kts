import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.codingfeline.buildkonfig")
}

buildkonfig {
    packageName = "com.example.newsapp"
    defaultConfigs {
        buildConfigField(STRING, "API_KEY", "159b885b18104900b02c921bebf1fce8")
    }
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


                // Material Icons
                implementation(libs.material.icons.extended)

                // Compose Multiplatform
                implementation(libs.compose.material3)

                // Compose navigation (multiplatform)
                implementation(libs.navigation.compose)

                //preview
                implementation(libs.jetbrains.ui.tooling.preview)
                implementation(libs.lifecycle.runtime.compose)

                //ktor
                implementation(libs.bundles.ktor)
                //koin
                implementation(libs.bundles.koin.common)
                //compose
                implementation(libs.bundles.compose.common)
                //lifecycle
                implementation(libs.bundles.lifecycle)
                //firebase/gitlive
                implementation(libs.bundles.firebase.common)
                //paging
                implementation(libs.bundles.paging)
                //room
                implementation(libs.bundles.room)
                //coil and coroutines
                implementation(libs.bundles.coil)
                implementation(libs.bundles.coroutines)

                //serialization
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
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

                //firebase
                implementation(project.dependencies.platform(libs.firebase.bom))
                //ktor
                implementation(libs.bundles.ktor.android)
                implementation(libs.bundles.koin.android)


                implementation(libs.androidx.work.runtime.ktx)
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

                implementation(libs.bundles.ktor.ios)
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