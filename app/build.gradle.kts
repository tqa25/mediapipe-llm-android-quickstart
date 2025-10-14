plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.quickstart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.quickstart"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Cho streaming token lâu
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // giữ mặc định
        }
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources.excludes += "META-INF/*"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.mediapipe:tasks-genai:0.10.27")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
}
