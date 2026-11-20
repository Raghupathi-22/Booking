plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.hyderabad.booking.partner"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hyderabad.booking.partner"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 10.0.2.2 = your computer when running on the Android emulator.
            // On a real phone use your computer's Wi-Fi IP, e.g. -PbookingBaseUrl=http://192.168.1.5:8080/
            val baseUrl = (project.findProperty("bookingBaseUrl") as String?) ?: "http://10.0.2.2:8080/"
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
        release {
            // TODO: replace with your real HTTPS API address before publishing.
            val baseUrl = (project.findProperty("bookingBaseUrl") as String?) ?: "https://api.example.com/"
            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
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
    implementation(project(":android-common"))

    // Room is set up for the planned offline cache (not used yet).
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
