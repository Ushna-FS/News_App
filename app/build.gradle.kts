plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose")
}

val apiKey: String = project.findProperty("NEWS_API_KEY")?.toString() ?: ""

android {
    namespace = "com.example.newsapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.newsapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "NEWS_API_KEY",
            "\"$apiKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material.v1100)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout.v214)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Gson for JSON parsing
    implementation(libs.gson)

    // Coroutines for async operations
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // RecyclerView for listing news
    implementation(libs.androidx.recyclerview)

    implementation(libs.androidx.cardview)
    implementation(libs.glide)

    implementation(libs.hilt.android)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.common)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.splashscreen)

    // For ViewModel injection
    implementation(libs.androidx.hilt.navigation.fragment)

    //  implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.runtime.ktx.v320)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Compose Core
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.ui.tooling.preview)

    // Debug
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Activity Compose
    implementation(libs.androidx.activity.compose)
    // Hilt + Compose
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android.v259)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler.v110)
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Coil for Compose (image loading)
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Firebase BOM
    implementation(platform(libs.firebase.bom.v3351))

    // Firebase Auth
    implementation(libs.google.firebase.auth.ktx)
    //firestore
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.work.runtime.ktx)


    //crashlytics
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)


    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
}