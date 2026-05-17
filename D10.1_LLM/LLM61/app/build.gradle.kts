import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(FileInputStream(localFile))
    }
}

android {
    namespace = "com.example.llm61"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.llm61"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Auth0 manifest placeholders — the SDK uses these to wire its RedirectActivity intent-filter automatically
        manifestPlaceholders["auth0Domain"] = "dev-n46ouw0sfbtjlodd.us.auth0.com"
        manifestPlaceholders["auth0Scheme"] = "demo"

        // BuildConfig fields for runtime
        buildConfigField(
            "String",
            "AUTH0_DOMAIN",
            "\"${localProperties.getProperty("AUTH0_DOMAIN", "")}\""
        )

        buildConfigField(
            "String",
            "AUTH0_CLIENT_ID",
            "\"${localProperties.getProperty("AUTH0_CLIENT_ID", "")}\""
        )

        // Expose the Gemini API key from local.properties as BuildConfig.GEMINI_API_KEY
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\""
        )

        buildConfigField(
            "String",
            "STRIPE_PUBLISHABLE_KEY",
            "\"${localProperties.getProperty("STRIPE_PUBLISHABLE_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "VERCEL_BACKEND_URL",
            "\"${localProperties.getProperty("VERCEL_BACKEND_URL", "")}\""
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)          // ← room-compiler lives here
    implementation(libs.stripe.android)
    implementation(libs.zxing.embedded)
    implementation(libs.auth0.android)
    implementation(libs.auth0.jwtdecode)
}