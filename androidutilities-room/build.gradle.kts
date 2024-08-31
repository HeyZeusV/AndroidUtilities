import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library") version "8.5.0"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
}

android {
    namespace = "com.heyzeusv.androidutilities.room"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.set(listOf("-Xcontext-receivers"))
    }
}

dependencies {
    // KotlinPoet
    implementation("com.squareup:kotlinpoet:1.15.0")
    implementation("com.squareup:kotlinpoet-ksp:1.15.0")

    // KSP
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
}