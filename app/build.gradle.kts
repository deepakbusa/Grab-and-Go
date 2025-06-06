plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Google services Gradle plugin
}

android {
    namespace = "com.example.grabandgo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.grabandgo"
        minSdk = 30
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

    // Optional: Enable View Binding (Recommended for easier view management)
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.gson)
    implementation("androidx.appcompat:appcompat:1.6.1") // Latest stable version
    implementation("androidx.core:core-ktx:1.12.0") // Latest stable version
    implementation("androidx.activity:activity-ktx:1.7.2") // Latest stable version
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Latest stable version
    implementation("com.github.bumptech.glide:glide:4.11.0")

    // Material Design
    implementation("com.google.android.material:material:1.10.0") // Updated to latest stable version

    // Firebase BoM (Bill of Materials) ensures compatible Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:33.4.0")) // Check for newer versions if available

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth") // Version managed by BoM

    // Firebase UI Auth for Google Sign-In
    implementation("com.firebaseui:firebase-ui-auth:8.0.2") // Latest stable version

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.play.services.analytics.impl) // Latest stable version

    // Testing Libraries
    testImplementation("junit:junit:4.13.2") // Latest stable version
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Latest stable version
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Latest stable version

    // Optional: Lifecycle Libraries (Recommended for better app architecture)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.8.0")






}
