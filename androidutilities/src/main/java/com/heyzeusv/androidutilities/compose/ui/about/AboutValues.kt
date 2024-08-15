package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

object AboutDefaults {

    @Composable
    fun aboutColors(
        libraryHeaderColor: Color = MaterialTheme.colorScheme.onSurface,
        libraryColors: LibraryColors = LibraryDefaults.libraryColors()
    ): AboutColors = DefaultAboutColors(
        libraryHeaderColor = libraryHeaderColor,
        libraryColors = libraryColors
    )

    @Composable
    fun aboutPadding(
        libraryPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
    ): AboutPadding = DefaultAboutPadding(
        libraryPadding = libraryPadding,
    )

    @Composable
    fun aboutDimensions(
        libraryDimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    ): AboutDimensions = DefaultAboutDimensions(
        libraryDimensions = libraryDimensions,
    )

    @Composable
    fun aboutTextStyles(
        libraryHeaderStyle: TextStyle = MaterialTheme.typography.headlineLarge,
        libraryStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    ): AboutTextStyles = DefaultAboutTextStyles(
        libraryHeaderStyle = libraryHeaderStyle,
        libraryStyles = libraryStyles,
    )
}

@Stable
interface AboutColors {
    val libraryHeaderColor: Color
    val libraryColors: LibraryColors
}

@Immutable
private data class DefaultAboutColors(
    override val libraryHeaderColor: Color,
    override val libraryColors: LibraryColors,
) : AboutColors

@Stable
interface AboutPadding {
    val libraryPadding: LibraryPadding
}

@Immutable
private data class DefaultAboutPadding(
    override val libraryPadding: LibraryPadding,
) : AboutPadding

@Stable
interface AboutDimensions {
    val libraryDimensions: LibraryDimensions
}

@Immutable
private data class DefaultAboutDimensions(
    override val libraryDimensions: LibraryDimensions
) : AboutDimensions

@Stable
interface AboutTextStyles {
    val libraryHeaderStyle: TextStyle
    val libraryStyles: LibraryTextStyles
}

@Immutable
private data class DefaultAboutTextStyles(
    override val libraryHeaderStyle: TextStyle,
    override val libraryStyles: LibraryTextStyles,
) : AboutTextStyles