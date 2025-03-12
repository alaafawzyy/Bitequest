plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
   id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.bitequest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.bitequest"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    implementation(libs.play.services.location)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("com.google.android.gms:play-services-maps:18.2.0") // Google Maps
    implementation ("com.google.maps.android:maps-compose:2.12.0") // Jetpack Compose for Google Maps
    implementation ("com.google.firebase:firebase-auth-ktx:22.2.0") // Firebase Authentication
    implementation ("com.google.firebase:firebase-firestore-ktx:24.9.0") // Firestore Database
    implementation ("com.google.firebase:firebase-storage-ktx:20.3.0") // Firebase Storage
    implementation ("androidx.navigation:navigation-compose:2.7.4") // Navigation
    implementation ("androidx.work:work-runtime-ktx:2.8.1") // WorkManager
    implementation ("androidx.compose.material:material-icons-core:1.5.3")
    implementation ("androidx.compose.material:material-icons-extended:1.5.3")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("com.google.maps.android:maps-compose:2.12.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.16")
    implementation ("androidx.compose.ui:ui:1.6.1")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("io.coil-kt:coil-compose:2.4.0")
// Retrofit للتعامل مع Imgur API
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Glide لعرض الصور (اختياري)
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    // Permissions
    implementation ("androidx.activity:activity-compose:1.8.0")


    implementation ("com.google.android.gms:play-services-location:21.0.1")
}