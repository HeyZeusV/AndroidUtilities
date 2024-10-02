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
    implementation("com.squareup:kotlinpoet:1.15.0")
    implementation("com.squareup:kotlinpoet-ksp:1.15.0")

    // KSP
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.6.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
}