plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
hilt {
    enableAggregatingTask = false
}


android {
    namespace = "com.example.sfa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.sfa"
        minSdk = 24
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

            resValue ("string", "google_maps_api_key", "AIzaSyBn9eGybmgpvAp7MXbG1b1i1ODBo0YRruM")
        }
        debug{
            resValue ("string", "google_maps_api_key", "AIzaSyBn9eGybmgpvAp7MXbG1b1i1ODBo0YRruM")
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.firebase.core)
    implementation (libs.firebase.crashlytics)
    implementation (libs.firebase.analytics)
    implementation (libs.firebase.inappmessaging.display)
    implementation (libs.firebase.messaging.directboot)
    implementation (libs.firebase.messaging)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.places)
    //implementation(libs.androidx.lifecycle.process)
    implementation (libs.androidx.lifecycle.extensions)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Hilt core
    implementation("com.google.dagger:hilt-android:2.56")
    kapt("com.google.dagger:hilt-android-compiler:2.56")
    // Hilt for ViewModel (optional)
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    // Hilt for Navigation (optional)
    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")


    implementation(libs.retrofit)
    // Gson converter for Retrofit
    implementation(libs.retrofit2.converter.gson)
    // OkHttp logging interceptor for debugging network requests
    implementation(libs.logging.interceptor)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.glide)
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.fragment:fragment-ktx:1.5.2")
    implementation ("androidx.navigation:navigation-compose:2.5.1")
    implementation (libs.google.maps.services)
    implementation ("com.google.maps.android:android-maps-utils:0.5")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
  







}
kapt {
    correctErrorTypes = true
}
