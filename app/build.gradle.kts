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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"  // Add this line
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
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.splashscreen)

    // For ViewModel injection
    implementation(libs.androidx.hilt.navigation.fragment)

//    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.runtime.ktx.v320)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation (libs.androidx.compose.ui.tooling.preview.v150)
    debugImplementation (libs.androidx.compose.ui.tooling)

    // Hilt + Compose
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android.v259)
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Coil for Compose (image loading)
    implementation(libs.coil.compose)
    //icons
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
}