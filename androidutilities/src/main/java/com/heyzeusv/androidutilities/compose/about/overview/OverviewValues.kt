package com.heyzeusv.androidutilities.compose.about.overview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
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
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorColors
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorDefaults
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorExtras
import com.heyzeusv.androidutilities.compose.about.library.LibraryColors
import com.heyzeusv.androidutilities.compose.about.library.LibraryDefaults
import com.heyzeusv.androidutilities.compose.about.library.LibraryDetails
import com.heyzeusv.androidutilities.compose.about.library.LibraryExtras
import com.heyzeusv.androidutilities.compose.about.library.LibraryPadding
import com.heyzeusv.androidutilities.compose.about.library.LibraryTextStyles
import com.heyzeusv.androidutilities.compose.pagerindicator.HorizontalPagerIndicator

/**
 *  Contains the defaults values used by [AboutOverview].
 */
object OverviewDefaults {
    private val ItemSpacing = 12.dp
    private val AppInfoItemSpacing = 8.dp
    private val InfoHeight = 125.dp
    private val DividerThickness = 2.dp
    private val ContentPadding = 16.dp

    private val AppInfoPadding =
        PaddingValues(start = ContentPadding, top = ContentPadding, end = ContentPadding)
    private val LibraryListPadding =
        PaddingValues(start = ContentPadding, end = ContentPadding, bottom = ContentPadding)

    /**
     *  Creates an [OverviewColors] that represents the colors used by [AboutOverview].
     *
     *  @param backgroundColor Color of background surface.
     *  @param titleColor Color of title Text.
     *  @param versionColor Color of version Text.
     *  @param infoColor Color of each Text in [HorizontalPager].
     *  @param pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
     *  @param dividerColor Color of Divider.
     *  @param libraryHeaderColor Color of Text between each group of libraries.
     *  @param libraryItemColors Colors used by [LibraryDetails].
     */
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

    /**
     *  Creates an [OverviewPadding] that represents the padding used by [AboutOverview] Composables.
     *
     *  @param appInfoPadding Padding values of [AppInfo].
     *  @param titlePadding Padding values of title Text.
     *  @param versionPadding Padding values of version Text.
     *  @param infoPadding Padding values of [HorizontalPager].
     *  @param pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
     *  @param dividerPadding Padding values of Divider.
     *  @param libraryListPadding Padding values of [LibraryList].
     *  @param libraryItemPadding Padding values of [LibraryDetails].
     */
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

    /**
     *  Creates an [OverviewExtras] that represents various values used by [AboutOverview].
     *
     *  @param itemSpacing Vertical spacing between [AppInfo] and [LibraryList] in [Dp].
     *  @param appInfoItemSpacing Vertical spacing between [AppInfo] Composables in [Dp].
     *  @param infoHeight The height of [HorizontalPager] in [AppInfo] in [Dp].
     *  @param pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
     *  @param dividerThickness Thickness of Divider in [Dp].
     *  @param libraryItemExtras Values used by [LibraryDetails].
     */
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

    /**
     *  Creates an [OverviewTextStyles] that represents the text styles used by [AboutOverview].
     *
     *  @param titleStyle Text style used by title Text.
     *  @param versionStyle Text style used by version Text.
     *  @param infoStyle Text style used by each Text in [HorizontalPager].
     *  @param libraryHeaderStyle Text style used by each Text separating libraries.
     *  @param libraryItemStyles Text style used by [LibraryDetails].
     */
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

/**
 *  Represents the colors used by [AboutOverview].
 *
 *  @property backgroundColor Color of background surface.
 *  @property titleColor Color of title Text.
 *  @property versionColor Color of version Text.
 *  @property infoColor Color of each Text in [HorizontalPager].
 *  @property pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
 *  @property dividerColor Color of Divider.
 *  @property libraryHeaderColor Color of Text between each group of libraries.
 *  @property libraryItemColors Colors used by [LibraryDetails].
 */
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

/**
 *  Default [OverviewColors].
 *
 *  @param backgroundColor Color of background surface.
 *  @param titleColor Color of title Text.
 *  @param versionColor Color of version Text.
 *  @param infoColor Color of each Text in [HorizontalPager].
 *  @param pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
 *  @param dividerColor Color of Divider.
 *  @param libraryHeaderColor Color of Text between each group of libraries.
 *  @param libraryItemColors Colors used by [LibraryDetails].
 */
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

/**
 *  Represents paddings used by [AboutOverview].
 *
 *  @property appInfoPadding Padding values of [AppInfo].
 *  @property titlePadding Padding values of title Text.
 *  @property versionPadding Padding values of version Text.
 *  @property infoPadding Padding values of [HorizontalPager].
 *  @property pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
 *  @property dividerPadding Padding values of Divider.
 *  @property libraryListPadding Padding values of [LibraryList].
 *  @property libraryItemPadding Padding values of [LibraryDetails].
 */
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

/**
 *  Default [OverviewPadding].
 *
 *  @param appInfoPadding Padding values of [AppInfo].
 *  @param titlePadding Padding values of title Text.
 *  @param versionPadding Padding values of version Text.
 *  @param infoPadding Padding values of [HorizontalPager].
 *  @param pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
 *  @param dividerPadding Padding values of Divider.
 *  @param libraryListPadding Padding values of [LibraryList].
 *  @param libraryItemPadding Padding values of [LibraryDetails].
 */
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

/**
 *  Represents various values used by [AboutOverview].
 *
 *  @property itemSpacing Vertical spacing between [AppInfo] and [LibraryList] in [Dp].
 *  @property appInfoItemSpacing Vertical spacing between [AppInfo] Composables in [Dp].
 *  @property infoHeight The height of [HorizontalPager] in [AppInfo] in [Dp].
 *  @property pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
 *  @property dividerThickness Thickness of Divider in [Dp].
 *  @property libraryItemExtras Values used by [LibraryDetails].
 */
@Stable
interface OverviewExtras {
    val itemSpacing: Dp
    val appInfoItemSpacing: Dp
    val infoHeight: Dp
    val pagerIndicatorExtras: PagerIndicatorExtras
    val dividerThickness: Dp
    val libraryItemExtras: LibraryExtras
}

/**
 *  Default [OverviewExtras].
 *
 *  @param itemSpacing Vertical spacing between [AppInfo] and [LibraryList] in [Dp].
 *  @param appInfoItemSpacing Vertical spacing between [AppInfo] Composables in [Dp].
 *  @param infoHeight The height of [HorizontalPager] in [AppInfo] in [Dp].
 *  @param pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
 *  @param dividerThickness Thickness of Divider in [Dp].
 *  @param libraryItemExtras Values used by [LibraryDetails].
 */
@Immutable
private data class DefaultOverviewExtras(
    override val itemSpacing: Dp,
    override val appInfoItemSpacing: Dp,
    override val infoHeight: Dp,
    override val pagerIndicatorExtras: PagerIndicatorExtras,
    override val dividerThickness: Dp,
    override val libraryItemExtras: LibraryExtras,
) : OverviewExtras

/**
 *  Represents text styles used by [AboutOverview].
 *
 *  @property titleStyle Text style used by title Text.
 *  @property versionStyle Text style used by version Text.
 *  @property infoStyle Text style used by each Text in [HorizontalPager].
 *  @property libraryHeaderStyle Text style used by each Text separating libraries.
 *  @property libraryItemStyles Text style used by [LibraryDetails].
 */
@Stable
interface OverviewTextStyles {
    val titleStyle: TextStyle
    val versionStyle: TextStyle
    val infoStyle: TextStyle
    val libraryHeaderStyle: TextStyle
    val libraryItemStyles: LibraryTextStyles
}

/**
 *  Default [OverviewTextStyles].
 *
 *  @param titleStyle Text style used by title Text.
 *  @param versionStyle Text style used by version Text.
 *  @param infoStyle Text style used by each Text in [HorizontalPager].
 *  @param libraryHeaderStyle Text style used by each Text separating libraries.
 *  @param libraryItemStyles Text style used by [LibraryDetails].
 */
@Immutable
private data class DefaultOverviewTextStyles(
    override val titleStyle: TextStyle,
    override val versionStyle: TextStyle,
    override val infoStyle: TextStyle,
    override val libraryHeaderStyle: TextStyle,
    override val libraryItemStyles: LibraryTextStyles,
) : OverviewTextStyles