plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.srpski.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.srpski.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2-M6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // YouTube Data API key (metadata enrichment for shared clips).
        // Set via YOUTUBE_API_KEY env var or a `youtube.apiKey` gradle property.
        val youtubeKey = providers.gradleProperty("youtube.apiKey").orNull
            ?: providers.environmentVariable("YOUTUBE_API_KEY").orNull
            ?: ""
        buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeKey\"")

        // Base URL for the content pack index (GitHub Pages).
        buildConfigField(
            "String",
            "CONTENT_PACK_INDEX_URL",
            "\"https://darionsandri.github.io/srpski-content/packs/index.json\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.youtube.player.core)
    implementation(libs.jsoup)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)
}
