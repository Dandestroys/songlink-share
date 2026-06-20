plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.songlink.share"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.songlink.share"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations += "en"

    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (System.getenv("KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = false
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
            excludes += "kotlin-tooling-metadata.json"
            excludes += "kotlin/**"
            excludes += "META-INF/versions/**"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/*.version"
            excludes += "META-INF/services/**"
            excludes += "META-INF/proguard/**"
            excludes += "META-INF/version-control-info.textproto"
            excludes += "**/*.kotlin_builtins"
            excludes += "**/*.kotlin_metadata"
            excludes += "META-INF/com.android.tools/**"
        }
        jniLibs {
            excludes += "**/x86/**"
            excludes += "**/x86_64/**"
            excludes += "**/armeabi-v7a/**"
        }
    }

}

dependencies {
    // Pure Java, no external dependencies!
}

