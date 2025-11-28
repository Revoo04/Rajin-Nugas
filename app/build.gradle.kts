plugins {
    id("com.android.application")
    // Kita ikutin versi Android Studio kamu (2.0.21)
    id("org.jetbrains.kotlin.android") version "2.0.21"
    // Kita update KSP ke versi yang jodoh sama 2.0.21
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.kelompok9.rajintugas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kelompok9.rajintugas"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // Biasanya Room butuh minimal 1.8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // --- INI BAGIAN DATABASE (ROOM) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // --- INI BAWAAN ANDROID STUDIO ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}