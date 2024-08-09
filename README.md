[![](https://jitpack.io/v/HeyZeusV/AndroidUtilities.svg)](https://jitpack.io/#HeyZeusV/AndroidUtilities)

# Android Utilities

A collection of Android utilities I find myself reusing across my apps. 

## Compose Resources

Composable resource functions, ie stringResource(), have longer than necessary names (in my eyes...).

[ComposeResources.kt][1] contains all variants of Composable resource functions, but with shortened names.
That's it... They save space! :)

I personally use them in combination with Live Templates available on Android Studio (not sure about other IDEs).
If you know the location of your Android Studio configuration directory, you can copy [ComposeResources.xml][2] into your templates folder.
Or you can import [ComposeResources.zip][3] using File -> Manage IDE Settings -> Import Settings... (Does not edit any settings, only adds Live Templates).

<img src="/images/ComposeResourcesSample.gif" width="225" height="500"/>

[1]: /androidutilities/src/main/java/com/heyzeusv/androidutilities/compose/util/ComposeResources.kt
[2]: /livetemplates/ComposeResources.xml
[3]: /livetemplates/ComposeResources.zip

## Installation

**Step 1.** Add JitPack repository to root build.gradle
```kotlin
allprojects {
    repositories {
        // ...
        maven { url = uri("https://jitpack.io") }
    }
}
```
or to settings.gradle (depending on your configuration)
```kotlin
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Step 2.** Add dependency to your module's build.gradle
```kotlin
dependencies {
         implementation ("com.github.HeyZeusV:AndroidUtilities:$androidUtilitiesVersion")
 }
```