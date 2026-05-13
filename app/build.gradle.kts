plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
        create("debugCI") {
            val keystorePath = System.getenv("DEBUG_KEYSTORE_FILE")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("DEBUG_STORE_PASSWORD")
                keyAlias = System.getenv("DEBUG_KEY_ALIAS")
                keyPassword = System.getenv("DEBUG_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = if (System.getenv("DEBUG_KEYSTORE_FILE") != null)
                signingConfigs.getByName("debugCI")
            else
                signingConfigs.getByName("debug")
        }
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
        viewBinding = true
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

if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
    tasks.register("prepareKotlinBuildScriptModel")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.recyclerview)
}

// profileinstaller is a dev tool for benchmarking that comes in transitively.
// Its compiled baseline.prof references pre-R8 method names that no longer exist
// after minification, which can cause ART dexopt to fail during installation.
configurations.configureEach {
    exclude(group = "androidx.profileinstaller")
}
