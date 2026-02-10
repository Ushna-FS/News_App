plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id ("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig=true
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

    implementation (libs.hilt.android)
    implementation(libs.androidx.activity)
    ksp (libs.hilt.compiler)

    implementation(libs.androidx.core.splashscreen)

    // For ViewModel injection
    implementation(libs.androidx.hilt.navigation.fragment)
    annotationProcessor(libs.androidx.hilt.compiler)

    implementation(libs.androidx.paging.runtime.ktx)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
}