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
    implementation(libs.androidx.activity)
    // --- DATABASE (ROOM) ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // --- DESAIN & TAMPILAN (CardView & ConstraintLayout) ---
    // Ini yang tadi bikin error karena kurang!
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // --- SISTEM UTAMA ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // --- TESTING (Bawaan) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}