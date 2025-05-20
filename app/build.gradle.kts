plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.brokskeeping"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.brokskeeping"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\hive_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\note_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\qr_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\station_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\to_do_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\log_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\other_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\inspection_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\inspection_data_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\honey_harvest_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\history_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\discarded_entities_layouts")
                srcDirs("src\\main\\res", "src\\main\\res\\layouts", "src\\main\\res\\layouts\\supplemented_feed_layouts")
            }
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("androidx.fragment:fragment:1.8.6")
    implementation("com.google.firebase:firebase-inappmessaging:21.0.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

