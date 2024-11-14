plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.application.parkpilotreg"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.application.parkpilotreg"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /* Add Third party dependencies here. (don't forget to drop the comment) */

    // country code picker
    implementation("com.hbb20:ccp:2.7.0")

    // OTP Box view
    implementation("io.github.chaosleung:pinview:1.4.4")

    // kotlin coroutine library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // view model dependency
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // add the dependency for the Google Play services library and specify its version (google sign in)
    implementation(libs.play.services.auth)

    // openStreetMap
    implementation(libs.osmdroid.android)

    // image caching and downloading (Co-routine image loader)
    implementation("io.coil-kt:coil:2.7.0")

    // fused location provider to get last known location
    implementation(libs.play.services.location)

    // avatar image generator
    implementation(libs.avatarimagegenerator)

    // shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}