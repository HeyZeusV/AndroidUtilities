import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
//    id("java-library")
//    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    kotlin("jvm") // version "2.0.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
    jvmToolchain(17)
}
dependencies {
    // KotlinPoet
    implementation("com.squareup:kotlinpoet:1.15.0")
    implementation("com.squareup:kotlinpoet-ksp:1.15.0")

    // KSP
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
}