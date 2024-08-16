package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.compose.ui.PagerIndicatorColors
import com.heyzeusv.androidutilities.compose.ui.PagerIndicatorDefaults
import com.heyzeusv.androidutilities.compose.ui.PagerIndicatorDimensions

object AboutDefaults {
    private val ItemSpacing = 12.dp
    private val AppInfoItemSpacing = 8.dp
    private val InfoHeight = 100.dp
    private val DividerThickness = 2.dp
    private val ItemPadding = 16.dp
    private val LibraryItemSpacing = 8.dp

    @Composable
    fun aboutColors(
        titleColor: Color = MaterialTheme.colorScheme.onSurface,
        versionColor: Color = MaterialTheme.colorScheme.onSurface,
        infoColor: Color = MaterialTheme.colorScheme.onSurface,
        pagerIndicatorColors: PagerIndicatorColors = PagerIndicatorDefaults.pagerIndicatorColors(),
        dividerColor: Color = MaterialTheme.colorScheme.onSurface,
        libraryHeaderColor: Color = MaterialTheme.colorScheme.onSurface,
        libraryColors: LibraryColors = LibraryDefaults.libraryColors()
    ): AboutColors = DefaultAboutColors(
        titleColor = titleColor,
        versionColor = versionColor,
        infoColor = infoColor,
        pagerIndicatorColors = pagerIndicatorColors,
        dividerColor = dividerColor,
        libraryHeaderColor = libraryHeaderColor,
        libraryColors = libraryColors
    )

    @Composable
    fun aboutPadding(
        contentPadding: PaddingValues = PaddingValues(ItemPadding),
        titlePadding: PaddingValues = PaddingValues(),
        versionPadding: PaddingValues = PaddingValues(),
        infoPadding: PaddingValues = PaddingValues(),
        pageIndicatorPadding: PaddingValues = PaddingValues(),
        libraryPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
    ): AboutPadding = DefaultAboutPadding(
        contentPadding = contentPadding,
        titlePadding = titlePadding,
        versionPadding = versionPadding,
        infoPadding = infoPadding,
        pageIndicatorPadding = pageIndicatorPadding,
        libraryPadding = libraryPadding,
    )

    @Composable
    fun aboutDimensions(
        itemSpacing: Dp = ItemSpacing,
        appInfoItemSpacing: Dp = AppInfoItemSpacing,
        infoHeight: Dp = InfoHeight,
        pagerIndicatorDimensions: PagerIndicatorDimensions = PagerIndicatorDefaults.pagerIndicatorDimensions(),
        dividerThickness: Dp = DividerThickness,
        libraryItemSpacing: Dp = LibraryItemSpacing,
        libraryDimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    ): AboutDimensions = DefaultAboutDimensions(
        itemSpacing = itemSpacing,
        appInfoItemSpacing = appInfoItemSpacing,
        infoHeight = infoHeight,
        pagerIndicatorDimensions = pagerIndicatorDimensions,
        dividerThickness = dividerThickness,
        libraryDimensions = libraryDimensions,
        libraryItemSpacing = libraryItemSpacing,
    )

    @Composable
    fun aboutTextStyles(
        titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        versionStyle: TextStyle = MaterialTheme.typography.titleMedium,
        infoStyle: TextStyle = MaterialTheme.typography.bodyMedium,
        libraryHeaderStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center
        ),
        libraryStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    ): AboutTextStyles = DefaultAboutTextStyles(
        titleStyle = titleStyle,
        versionStyle = versionStyle,
        infoStyle = infoStyle,
        libraryHeaderStyle = libraryHeaderStyle,
        libraryStyles = libraryStyles,
    )
}

@Stable
interface AboutColors {
    val titleColor: Color
    val versionColor: Color
    val infoColor: Color
    val pagerIndicatorColors: PagerIndicatorColors
    val dividerColor: Color
    val libraryHeaderColor: Color
    val libraryColors: LibraryColors
}

@Immutable
private data class DefaultAboutColors(
    override val titleColor: Color,
    override val versionColor: Color,
    override val infoColor: Color,
    override val pagerIndicatorColors: PagerIndicatorColors,
    override val dividerColor: Color,
    override val libraryHeaderColor: Color,
    override val libraryColors: LibraryColors,
) : AboutColors

@Stable
interface AboutPadding {
    val contentPadding: PaddingValues
    val titlePadding: PaddingValues
    val versionPadding: PaddingValues
    val infoPadding: PaddingValues
    val pageIndicatorPadding: PaddingValues
    val libraryPadding: LibraryPadding
}

@Immutable
private data class DefaultAboutPadding(
    override val contentPadding: PaddingValues,
    override val titlePadding: PaddingValues,
    override val versionPadding: PaddingValues,
    override val infoPadding: PaddingValues,
    override val pageIndicatorPadding: PaddingValues,
    override val libraryPadding: LibraryPadding,
) : AboutPadding

@Stable
interface AboutDimensions {
    val itemSpacing: Dp
    val appInfoItemSpacing: Dp
    val infoHeight: Dp
    val pagerIndicatorDimensions: PagerIndicatorDimensions
    val dividerThickness: Dp
    val libraryItemSpacing: Dp
    val libraryDimensions: LibraryDimensions
}

@Immutable
private data class DefaultAboutDimensions(
    override val itemSpacing: Dp,
    override val appInfoItemSpacing: Dp,
    override val infoHeight: Dp,
    override val pagerIndicatorDimensions: PagerIndicatorDimensions,
    override val dividerThickness: Dp,
    override val libraryItemSpacing: Dp,
    override val libraryDimensions: LibraryDimensions,
) : AboutDimensions

@Stable
interface AboutTextStyles {
    val titleStyle: TextStyle
    val versionStyle: TextStyle
    val infoStyle: TextStyle
    val libraryHeaderStyle: TextStyle
    val libraryStyles: LibraryTextStyles
}

@Immutable
private data class DefaultAboutTextStyles(
    override val titleStyle: TextStyle,
    override val versionStyle: TextStyle,
    override val infoStyle: TextStyle,
    override val libraryHeaderStyle: TextStyle,
    override val libraryStyles: LibraryTextStyles,
) : AboutTextStyles