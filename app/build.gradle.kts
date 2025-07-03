import com.android.tools.r8.internal.kt

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

// ---- Load secrets.properties ----
val secretsPropsFile = rootProject.file("secrets.properties")
val secrets = Properties()
if (secretsPropsFile.exists()) {
    secrets.load(secretsPropsFile.inputStream())
}
val IMGUR_CLIENT_ID: String = secrets.getProperty("IMGUR_CLIENT_ID") ?: ""

android {
    namespace = "com.PlugPoint.plugpoint"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.PlugPoint.plugpoint"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

            // Expose secrets to BuildConfig
            buildConfigField("String", "IMGUR_CLIENT_ID", "\"$IMGUR_CLIENT_ID\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig= true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation (libs.kotlinx.coroutines.play.services)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.firebase.auth)
    implementation (libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    implementation (libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
