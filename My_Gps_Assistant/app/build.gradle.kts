plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
}

android {
    namespace = "com.mikhail_ryumkin_r.my_gps_assistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mikhail_ryumkin_r.my_gps_assistant"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        buildConfig = true
    }
}

configurations {
    all {
        exclude(module = "xpp3")
        exclude(group = "xpp3")
    }
}

dependencies {

    implementation(libs.osmdroid.android)
    implementation(libs.osmbonuspack)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.preference.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.room)
    implementation(libs.room.runtime)
    implementation(libs.navigation.common.ktx)
    implementation(libs.kotlin.safeargs)
    implementation(libs.xmlpull)
    implementation(libs.org.jetbrains.kotlin)
    implementation(libs.org.jetbrains.kotlinx)
    implementation(libs.libraries.bom)
    ksp(libs.ksp)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}