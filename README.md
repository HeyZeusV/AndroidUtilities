
# Android Utilities

A collection of Android utilities I find myself reusing across my apps.

## About Screen

Full screen Composable that splits the screen into two parts. 

The top part displays information about the app: an icon (optional), name, version, and any other 
info (changelog, contact info, etc.).

The bottom part displays all libraries used with the help of [AboutLibraries][1], which does require
including its plugin (instructions below). Users can expand a library item to display its description
and its license.

<details><summary><b>Usage</b></summary>

Using below version generates list of libraries automatically with standard AboutLibraries plugin
settings.
```kotlin
AboutScreen(
    // button to navigate back
    backButton, // @Composable () -> Unit = { },
    // icon at the top of screen
    icon, // @Composable (BoxScope.() -> Unit)? = null,
    // text at the top of screen
    title, // String,
    // text below title
    version, // String,
    // info that is displayed on HorizontalPager
    infoList, // ImmutableList<InfoEntry>,
    // decides if libraries are split between first and third party libraries
    // or are all listed together
    separateByParty, // Boolean = true,
    // styling values
    overviewColors, // OverviewColors = OverviewDefaults.overviewColors(),
    overviewPadding, // OverviewPadding = OverviewDefaults.overviewPadding(),
    overviewExtras, // OverviewExtras = OverviewDefaults.overviewExtras(),
    overviewTextStyles, // OverviewTextStyles = OverviewDefaults.overviewTextStyles(),
    libraryColors, // LibraryColors = LibraryDefaults.libraryColors(),
    libraryPadding, // LibraryPadding = LibraryDefaults.libraryPadding(outerPadding = LibraryDefaults.ScreenOuterPV),
    libraryExtras, // LibraryExtras =LibraryDefaults.libraryExtras(actionIcon = pRes(R.drawable.icon_collapse)),
    libraryTextStyles, // LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
)
```

Using below version will require map of libraries to be displayed (use this if you want more control
over how libraries are separated).
```kotlin
AboutScreen(
    // ... 
    // libraries to be displayed separated by LibraryGroup
    libraries, // ImmutableMap<LibraryGroup, ImmutableList<Library>>,
    // ...
)
```

</details>
<details><summary><b>Screenshots</b></summary>

<img src="/images/AboutScreenExamples.png"/>

<img src="/images/AboutScreenSample.gif" width="225" height="500"/>
</details>

[1]: https://github.com/mikepenz/AboutLibraries

### AboutLibraries Plugin Installation
```kotlin
// Root build.gradle
id("com.mikepenz.aboutlibraries.plugin") version "$latestAboutLibsRelease" apply false

// App build.gradle
id("com.mikepenz.aboutlibraries.plugin")
```

## Compose Resources

Composable resource functions, ie stringResource(), have longer than necessary names (in my eyes...).

[ComposeResources.kt][2] contains all variants of Composable resource functions, but with shortened names.
That's it... They save space! :)

I personally use them in combination with Live Templates available on Android Studio (not sure about other IDEs).
If you know the location of your Android Studio configuration directory, you can copy [ComposeResources.xml][3] into your templates folder.
Or you can import [ComposeResources.zip][4] using File -> Manage IDE Settings -> Import Settings... (Does not edit any settings, only adds Live Templates).

<img src="/images/ComposeResourcesSample.gif" width="225" height="500"/>

[2]: /androidutilities/src/main/java/com/heyzeusv/androidutilities/compose/util/ComposeResources.kt
[3]: /livetemplates/ComposeResources.xml
[4]: /livetemplates/ComposeResources.zip

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

**Step 2.** Add dependency to your module's build.gradle
```kotlin
dependencies {
    implementation("io.github.heyzeusv:androidutilities:$androidUtilitiesVersion")
}
```