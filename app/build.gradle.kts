plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.example.readiumandroidtestapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.readiumandroidtestapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xannotation-default-target=param-property")

        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
            "-opt-in=org.readium.r2.shared.ExperimentalReadiumApi",
        )
    }
}

dependencies {
    // BOMs
    implementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Core & Kotlin
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // AndroidX & UI
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.google.material)

    // Architecture
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Feature Bundles
    implementation(libs.bundles.readium)
    implementation(libs.bundles.paging)
    implementation(libs.bundles.media3)
    implementation(libs.bundles.coil)

    // Compose
    implementation(libs.bundles.compose)
    implementation(libs.bundles.navigation)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Utilities
    implementation(libs.timber)

    // Debug
    debugImplementation(libs.androidx.compose.ui)

    // Tests
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test.unit)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    androidTestImplementation(libs.androidx.junit.ktx)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
