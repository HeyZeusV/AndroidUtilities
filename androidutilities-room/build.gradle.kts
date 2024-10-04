import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") // version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
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
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    // KSP
    implementation(libs.symbol.processing.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotlin.compile.testing.ksp)
}