package com.heyzeusv.androidutilities.compose.about.overview

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
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorColors
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorDefaults
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorExtras
import com.heyzeusv.androidutilities.compose.about.library.LibraryColors
import com.heyzeusv.androidutilities.compose.about.library.LibraryDefaults
import com.heyzeusv.androidutilities.compose.about.library.LibraryExtras
import com.heyzeusv.androidutilities.compose.about.library.LibraryPadding
import com.heyzeusv.androidutilities.compose.about.library.LibraryTextStyles

object OverviewDefaults {
    private val ItemSpacing = 12.dp
    private val AppInfoItemSpacing = 8.dp
    private val InfoHeight = 75.dp
    private val DividerThickness = 2.dp
    private val ContentPadding = 16.dp

    private val AppInfoPadding =
        PaddingValues(start = ContentPadding, top = ContentPadding, end = ContentPadding)
    private val LibraryListPadding =
        PaddingValues(start = ContentPadding, end = ContentPadding, bottom = ContentPadding)

    @Composable
    fun overviewColors(
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        titleColor: Color = MaterialTheme.colorScheme.onSurface,
        versionColor: Color = MaterialTheme.colorScheme.onSurface,
        infoColor: Color = MaterialTheme.colorScheme.onSurface,
        pagerIndicatorColors: PagerIndicatorColors = PagerIndicatorDefaults.pagerIndicatorColors(),
        dividerColor: Color = MaterialTheme.colorScheme.onSurface,
        libraryHeaderColor: Color = MaterialTheme.colorScheme.onSurface,
        libraryItemColors: LibraryColors = LibraryDefaults.libraryColors(),
    ): OverviewColors = DefaultOverviewColors(
        backgroundColor = backgroundColor,
        titleColor = titleColor,
        versionColor = versionColor,
        infoColor = infoColor,
        pagerIndicatorColors = pagerIndicatorColors,
        dividerColor = dividerColor,
        libraryHeaderColor = libraryHeaderColor,
        libraryItemColors = libraryItemColors
    )

    @Composable
    fun overviewPadding(
        appInfoPadding: PaddingValues = AppInfoPadding,
        titlePadding: PaddingValues = PaddingValues(),
        versionPadding: PaddingValues = PaddingValues(),
        infoPadding: PaddingValues = PaddingValues(),
        pageIndicatorPadding: PaddingValues = PaddingValues(),
        dividerPadding: PaddingValues = PaddingValues(bottom = ContentPadding),
        libraryListPadding: PaddingValues = LibraryListPadding,
        libraryItemPadding: LibraryPadding = LibraryDefaults.libraryPadding(),
    ): OverviewPadding = DefaultOverviewPadding(
        appInfoPadding = appInfoPadding,
        titlePadding = titlePadding,
        versionPadding = versionPadding,
        infoPadding = infoPadding,
        pageIndicatorPadding = pageIndicatorPadding,
        dividerPadding = dividerPadding,
        libraryListPadding = libraryListPadding,
        libraryItemPadding = libraryItemPadding,
    )

    @Composable
    fun overviewExtras(
        itemSpacing: Dp = ItemSpacing,
        appInfoItemSpacing: Dp = AppInfoItemSpacing,
        infoHeight: Dp = InfoHeight,
        pagerIndicatorExtras: PagerIndicatorExtras = PagerIndicatorDefaults.pagerIndicatorExtras(),
        dividerThickness: Dp = DividerThickness,
        libraryItemExtras: LibraryExtras = LibraryDefaults.libraryExtras(),
    ): OverviewExtras = DefaultOverviewExtras(
        itemSpacing = itemSpacing,
        appInfoItemSpacing = appInfoItemSpacing,
        infoHeight = infoHeight,
        pagerIndicatorExtras = pagerIndicatorExtras,
        dividerThickness = dividerThickness,
        libraryItemExtras = libraryItemExtras,
    )

    @Composable
    fun overviewTextStyles(
        titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        versionStyle: TextStyle = MaterialTheme.typography.titleMedium,
        infoStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        libraryHeaderStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center
        ),
        libraryItemStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
    ): OverviewTextStyles = DefaultOverviewTextStyles(
        titleStyle = titleStyle,
        versionStyle = versionStyle,
        infoStyle = infoStyle,
        libraryHeaderStyle = libraryHeaderStyle,
        libraryItemStyles = libraryItemStyles,
    )
}

@Stable
interface OverviewColors {
    val backgroundColor: Color
    val titleColor: Color
    val versionColor: Color
    val infoColor: Color
    val pagerIndicatorColors: PagerIndicatorColors
    val dividerColor: Color
    val libraryHeaderColor: Color
    val libraryItemColors: LibraryColors
}

@Immutable
private data class DefaultOverviewColors(
    override val backgroundColor: Color,
    override val titleColor: Color,
    override val versionColor: Color,
    override val infoColor: Color,
    override val pagerIndicatorColors: PagerIndicatorColors,
    override val dividerColor: Color,
    override val libraryHeaderColor: Color,
    override val libraryItemColors: LibraryColors,
) : OverviewColors

@Stable
interface OverviewPadding {
    val appInfoPadding: PaddingValues
    val titlePadding: PaddingValues
    val versionPadding: PaddingValues
    val infoPadding: PaddingValues
    val pageIndicatorPadding: PaddingValues
    val dividerPadding: PaddingValues
    val libraryListPadding: PaddingValues
    val libraryItemPadding: LibraryPadding
}

@Immutable
private data class DefaultOverviewPadding(
    override val appInfoPadding: PaddingValues,
    override val titlePadding: PaddingValues,
    override val versionPadding: PaddingValues,
    override val infoPadding: PaddingValues,
    override val pageIndicatorPadding: PaddingValues,
    override val dividerPadding: PaddingValues,
    override val libraryListPadding: PaddingValues,
    override val libraryItemPadding: LibraryPadding,
) : OverviewPadding

@Stable
interface OverviewExtras {
    val itemSpacing: Dp
    val appInfoItemSpacing: Dp
    val infoHeight: Dp
    val pagerIndicatorExtras: PagerIndicatorExtras
    val dividerThickness: Dp
    val libraryItemExtras: LibraryExtras
}

@Immutable
private data class DefaultOverviewExtras(
    override val itemSpacing: Dp,
    override val appInfoItemSpacing: Dp,
    override val infoHeight: Dp,
    override val pagerIndicatorExtras: PagerIndicatorExtras,
    override val dividerThickness: Dp,
    override val libraryItemExtras: LibraryExtras,
) : OverviewExtras

@Stable
interface OverviewTextStyles {
    val titleStyle: TextStyle
    val versionStyle: TextStyle
    val infoStyle: TextStyle
    val libraryHeaderStyle: TextStyle
    val libraryItemStyles: LibraryTextStyles
}

@Immutable
private data class DefaultOverviewTextStyles(
    override val titleStyle: TextStyle,
    override val versionStyle: TextStyle,
    override val infoStyle: TextStyle,
    override val libraryHeaderStyle: TextStyle,
    override val libraryItemStyles: LibraryTextStyles,
) : OverviewTextStyles