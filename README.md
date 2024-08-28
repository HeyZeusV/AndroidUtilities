
# Android Utilities

A collection of Android utilities I find myself reusing across my apps.

## About Screen
### Requires Kotlinx Immutable Collections (See installation instructions)

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

## Annotated Strings (Hyperlinks)
### Requires Kotlinx Immutable Collections (See installation instructions)

Quickly build Annotated Strings or Composable Texts with clickable hyperlinks.

<details><summary><b>Usage</b></summary>

Use the below versions if you want to use a regular string.
```kotlin
// returns AnnotatedString
hyperlinkAnnotatedString(
    // full string to be displayed
    text, // String,
    // style to be used by non-hyperlink text
    textStyle, // TextStyle,
    // style to be used by hyperlink text
    linkStyle, // TextStyle? = null,
    // map of link text to urls they link to
    // link text must match text found in text string passed 
    linkTextToHyperlinks, // ImmutableMap<String, String>,
    // further styling for hyperlink text
    linkTextColor, // Color = Color.Blue,
    linkTextFontWeight, // FontWeight = FontWeight.Normal,
    linkTextDecoration, // TextDecoration = TextDecoration.Underline,
)

// or

// Text Composable
HyperlinkText(
    modifier, // Modifier = Modifier,
    // ...
    // styling for Text Composable
    overflow, // TextOverflow = TextOverflow.Clip,
    softWrap, // Boolean = true,
    maxLines, // Int = Int.MAX_VALUE,
    minLines, // Int = 1,
)

```

Use the below versions if you want to use a string resource.
```xml
<!-- Hyperlink Annotation String -->
<string name="hyperlink_example">This string resource contains a link <annotation type="LINK1">HERE</annotation> and right <annotation type="LINK2">here!!!</annotation></string>
```

```kotlin
// returns AnnotationString
hyperlinkAnnotatedString(
    // used to retrieve string resource
    context, // Context
    // string resource to be displayed
    textId, // Int
    // ...
    // map for hyperlink_example shown above
    // persistentMapOf("LINK1" to "www.url.com, "LINK2" to "www.url2.com")
    linkTextToHyperlinks // ImmutableMap<String, String>
    // ...
)

// or

// Text Composable
HyperlinkText(
    modifier, // Modifier = Modifier
    // string resource to be displayed
    textId, // Int
    // ...
)
```

</details>
<details><summary><b>Screenshots</b></summary>
<img src="/images/HyperlinkSample.gif" width="225" height="500"/>
</details>

## Horizontal Pager Indicator

Google implemented Pagers, but did not implement PagerIndicators. This is taken from
[Accompanist][2].

<details><summary><b>Usage</b></summary>

```kotlin
HorizontalPagerIndicator(    
    // used to observe list state
    pagerState, // PagerState
    // number of indicators to be displayed
    pageCount, // Int
    modifier, // Modifier = Modifier
    // how to get position of current indicator if pageCount != pagerState.pageCount
    pageIndexMapping, // (Int) -> Int = { it }
    // indicator styling
    activeColor, // Color = LocalContentColor.current.copy(alpha = 0.9f)
    inactiveColor, // Color = activeColor.copy(alpha = 0.38f)
    indicatorWidth, // Dp = 8.dp
    indicatorHeight, // Dp = indicatorWidth
    indicatorSpacing, // Dp = indicatorWidth
    indicatorShape, // Shape = CircleShape
)
```

</details>
<details><summary><b>Screenshots</b></summary>
Watch HorizontalPagerIndicator in action under AboutScreen screenshots! 
</details>

[2]: https://github.com/google/accompanist/blob/main/pager-indicators/src/main/java/com/google/accompanist/pager/PagerIndicator.kt

## Compose Resources

Composable resource functions, ie stringResource(), have longer than necessary names (in my eyes...).

[ComposeResources.kt][3] contains all variants of Composable resource functions, but with shortened names.
That's it... They save space! :)

I personally use them in combination with Live Templates available on Android Studio (not sure about other IDEs).
If you know the location of your Android Studio configuration directory, you can copy [ComposeResources.xml][4] into your templates folder.
Or you can import [ComposeResources.zip][5] using File -> Manage IDE Settings -> Import Settings... (Does not edit any settings, only adds Live Templates).

<details><summary><b>Screenshots</b></summary>
<img src="/images/ComposeResourcesSample.gif" width="225" height="500"/>
</details>

[3]: /androidutilities/src/main/java/com/heyzeusv/androidutilities/compose/util/ComposeResources.kt
[4]: /livetemplates/ComposeResources.xml
[5]: /livetemplates/ComposeResources.zip

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

**Step 3.** Multiple features use [Kotlinx Immutable Collections][5] in order to make Composables
stable and skippable. Add dependency to your module's build.gradle
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxImmutableCollections")
}
```

[5]: https://github.com/Kotlin/kotlinx.collections.immutable