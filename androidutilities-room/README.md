# Room Utilities

KSP Annotation processor for Android Room that generates files for backup/restore and export/import
of database.

## Installation

**Step 1.** Add Maven Central to root build.gradle
```kotlin
allprojects {
    repositories {
        // ...
        mavenCentral()
    }
}
```
or to settings.gradle (depending on your configuration)
```kotlin
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        mavenCentral()
    }
}
```

**Step 2.** Add dependencies to your module's build.gradle
```kotlin
dependencies {
    implementation("io.github.heyzeusv:roomutilities:1.0.0")
}
```