plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.readiumandroidtestapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.readiumandroidtestapp"
        minSdk = 26
        targetSdk = 37
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        resValues = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_4
        allWarningsAsErrors = true

        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=org.readium.r2.shared.ExperimentalReadiumApi",
        )
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // --- BOMs ---
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // --- Core & Kotlin ---
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.browser)
    annotationProcessor(libs.kotlin.metadata.jvm)
    testAnnotationProcessor(libs.kotlin.metadata.jvm)
    androidTestAnnotationProcessor(libs.kotlin.metadata.jvm)

    // --- Async & Serialization ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // --- Architecture ---
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)

    // --- Dependency Injection (Hilt) ---
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Feature Bundles ---
    implementation(libs.bundles.readium)
//    implementation(libs.bundles.readium.navigator2)
    implementation(libs.bundles.paging)
    implementation(libs.bundles.media3)
    implementation(libs.bundles.coil)

    // --- Compose & Navigation ---
    implementation(libs.bundles.compose)
    implementation(libs.bundles.navigation)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- Database (Room) ---
    implementation(libs.androidx.room3)
    ksp(libs.androidx.room3.compiler)

    // --- Utilities ---
    implementation(libs.timber)

    // --- Testing ---
    // Unit Tests
    testImplementation(libs.bundles.test.unit)
    kspTest(libs.dagger.hilt.compiler)

    // Instrumentation Tests
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    kspAndroidTest(libs.dagger.hilt.compiler)
}

tasks.withType<Test> {
    jvmArgs("-noverify")
}

kover {
    reports {
        filters {
            excludes {
                // Standard Android/Hilt Excludes
                annotatedBy(
                    "dagger.internal.DaggerGenerated",
                    "dagger.hilt.codegen.OriginatingElement",
                    "javax.annotation.processing.Generated",
                    "androidx.compose.ui.tooling.preview.Preview",
                )
                classes(
                    // 1. Framework & Config
                    "**.BuildConfig",
                    "**.ReadiumApp",
                    "**.MainActivity",
                    "**.*Activity",
                    "**.*Application",
                    "**.*Module",
                    "**.Routes",
                    "**.NavEntryBuilder",

                    // 2. The "Wrappers" (Pass-throughs not being tested)
                    "**.*NavigatorFactoryWrapper",
                    "**.DefaultTtsNavigatorGateway",
                    "**.DefaultTtsServiceGateway",
                    "**.DefaultSearchGateway",
                    "**.DefaultPreferencesSerializerFactory",
                    "**.AndroidTtsNavigatorFactoryProvider",

                    // 3. System & Service Glue (Hard to test, low value)
                    "**.DefaultReaderMediaBinder*",
                    "**.MediaService*",

                    // 4. Data Class Boilerplate (equals/hashcode noise)
                    "**.HttpResult",

                    // 5. Generated / Dagger internals
                    "*ComposableSingletons*",
                    "dagger.hilt.internal.aggregatedroot.codegen.**",
                    "**.Dagger*",
                    "**.*_Factory*",
                    "**.Hilt_*",
                    "**.*_HiltModules*",
                    "hilt_aggregated_deps.**",
                    "**.*_Impl*",
                    "**.*_MembersInjector*",
                    "**.*_Provide*Factory*",
                    "com.example.readiumandroidtestapp.core.domain.model.*",
                    "com.example.readiumandroidtestapp.features.reader.ui.state.*",
                )
            }
        }
    }
}
