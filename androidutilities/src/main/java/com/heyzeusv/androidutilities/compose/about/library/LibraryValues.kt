package com.heyzeusv.androidutilities.compose.about.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorColors
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorDefaults
import com.heyzeusv.androidutilities.compose.about.PagerIndicatorExtras
import com.heyzeusv.androidutilities.compose.pagerindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.pRes

/**
 *  Contains the defaults values used by [AboutLibrary].
 */
object LibraryDefaults {
    private val ItemBorderWidth = 2.dp
    private val DividerThickness = 2.dp
    private val CornerRadius = 12.dp
    private val ContentPadding = 12.dp
    private val ContentSpacedBy = 8.dp
    private val ActionIconSize = 32.dp
    val ActionIconRippleRadius = 24.dp
    private val ScreenOuterPadding = 12.dp

    val ScreenOuterPV = PaddingValues(all = ScreenOuterPadding)
    private val ItemContentPV = PaddingValues(all = ContentPadding)

    /**
     *  Creates [LibraryColors] that represents the colors used by [AboutLibrary].
     *
     *  @param borderColor Color of border.
     *  @param backgroundColor Color of background surface.
     *  @param contentColor Color of text content.
     *  @param dividerColor Color of dividers.
     *  @param pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
     */
    @Composable
    fun libraryColors(
        borderColor: Color = MaterialTheme.colorScheme.onSurface,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        dividerColor: Color = MaterialTheme.colorScheme.onSurface,
        pagerIndicatorColors: PagerIndicatorColors = PagerIndicatorDefaults.pagerIndicatorColors(),
    ): LibraryColors = DefaultLibraryColors(
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        dividerColor = dividerColor,
        pagerIndicatorColors = pagerIndicatorColors,
    )

    /**
     *  Creates a [LibraryPadding] that represents the padding used by [AboutLibrary] Composables.
     *
     *  @param outerPadding Padding values of outside surface.
     *  @param innerPadding Padding values of inner content.
     *  @param namePadding Padding values of name Text.
     *  @param actionIconPadding Padding values of action Icon.
     *  @param developerPadding Padding values of developer Text.
     *  @param bodyPadding Padding values of body Text.
     *  @param footerPadding Padding values of footer Text.
     *  @param pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
     */
    @Composable
    fun libraryPadding(
        outerPadding: PaddingValues = PaddingValues(),
        innerPadding: PaddingValues = ItemContentPV,
        namePadding: PaddingValues = PaddingValues(),
        actionIconPadding: PaddingValues = PaddingValues(),
        developerPadding: PaddingValues = PaddingValues(),
        bodyPadding: PaddingValues = PaddingValues(),
        footerPadding: PaddingValues = PaddingValues(),
        pageIndicatorPadding: PaddingValues = PaddingValues(),
    ): LibraryPadding = DefaultLibraryPadding(
        outerPadding = outerPadding,
        innerPadding = innerPadding,
        namePadding = namePadding,
        actionIconPadding = actionIconPadding,
        developerPadding = developerPadding,
        bodyPadding = bodyPadding,
        footerPadding = footerPadding,
        pageIndicatorPadding = pageIndicatorPadding,
    )

    /**
     *  Creates a [LibraryExtras] that represents various values used by [AboutLibrary].
     *
     *  @param shape Shape of surface.
     *  @param borderWidth Thickness of border in [Dp].
     *  @param contentSpacedBy Vertical spacing between Composables in [Dp].
     *  @param dividerThickness Thickness of Dividers in [Dp].
     *  @param actionIcon Icon used for action button.
     *  @param actionIconSize Size of action Icon.
     *  @param pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
     */
    @Composable
    fun libraryExtras(
        shape: Shape = RoundedCornerShape(CornerRadius),
        borderWidth: Dp = ItemBorderWidth,
        contentSpacedBy: Dp = ContentSpacedBy,
        dividerThickness: Dp = DividerThickness,
        actionIcon: Painter = pRes(R.drawable.icon_expand),
        actionIconSize: Dp = ActionIconSize,
        pagerIndicatorExtras: PagerIndicatorExtras = PagerIndicatorDefaults.pagerIndicatorExtras(),
    ): LibraryExtras = DefaultLibraryExtras(
        shape = shape,
        borderWidth = borderWidth,
        contentSpacedBy = contentSpacedBy,
        dividerThickness = dividerThickness,
        actionIconSize = actionIconSize,
        actionIcon = actionIcon,
        pagerIndicatorExtras = pagerIndicatorExtras,
    )

    /**
     *  Creates a [LibraryTextStyles] that represents the text styles used by [AboutLibrary].
     *
     *  @param nameStyle Text style used by name Text.
     *  @param developerStyle Text style used by developer Text.
     *  @param bodyStyle Text style used by body Text.
     *  @param footerStyle Text style used by body Text.
     */
    @Composable
    fun libraryTextStyles(
        nameStyle: TextStyle = MaterialTheme.typography.headlineSmall,
        developerStyle: TextStyle = MaterialTheme.typography.titleMedium,
        bodyStyle: TextStyle = MaterialTheme.typography.bodyMedium,
        footerStyle: TextStyle = MaterialTheme.typography.titleMedium,
    ): LibraryTextStyles = DefaultLibraryTextStyles(
        nameStyle = nameStyle,
        developerStyle = developerStyle,
        bodyStyle = bodyStyle,
        footerStyle = footerStyle,
    )
}

/**
 *  Represents the colors used by [AboutLibrary].
 *
 *  @property borderColor Color of border.
 *  @property backgroundColor Color of background surface.
 *  @property contentColor Color of text content.
 *  @property dividerColor Color of dividers.
 *  @property pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
 */
@Stable
interface LibraryColors {
    val borderColor: Color
    val backgroundColor: Color
    val contentColor: Color
    val dividerColor: Color
    val pagerIndicatorColors: PagerIndicatorColors
}

/**
 *  Default [LibraryColors].
 *
 *  @param borderColor Color of border.
 *  @param backgroundColor Color of background surface.
 *  @param contentColor Color of text content.
 *  @param dividerColor Color of dividers.
 *  @param pagerIndicatorColors Colors used by [HorizontalPagerIndicator].
 */
@Immutable
private data class DefaultLibraryColors(
    override val borderColor: Color,
    override val backgroundColor: Color,
    override val contentColor: Color,
    override val dividerColor: Color,
    override val pagerIndicatorColors: PagerIndicatorColors,
) : LibraryColors

/**
 *  Represents paddings used by [AboutLibrary].
 *
 *  @property outerPadding Padding values of outside surface.
 *  @property innerPadding Padding values of inner content.
 *  @property namePadding Padding values of name Text.
 *  @property actionIconPadding Padding values of action Icon.
 *  @property developerPadding Padding values of developer Text.
 *  @property bodyPadding Padding values of body Text.
 *  @property footerPadding Padding values of footer Text.
 *  @property pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
 */
@Stable
interface LibraryPadding {
    val outerPadding: PaddingValues
    val innerPadding: PaddingValues
    val namePadding: PaddingValues
    val actionIconPadding: PaddingValues
    val developerPadding: PaddingValues
    val bodyPadding: PaddingValues
    val footerPadding: PaddingValues
    val pageIndicatorPadding: PaddingValues
}

/**
 *  Default [LibraryPadding].
 *
 *  @param outerPadding Padding values of outside surface.
 *  @param innerPadding Padding values of inner content.
 *  @param namePadding Padding values of name Text.
 *  @param actionIconPadding Padding values of action Icon.
 *  @param developerPadding Padding values of developer Text.
 *  @param bodyPadding Padding values of body Text.
 *  @param footerPadding Padding values of footer Text.
 *  @param pageIndicatorPadding Padding values of [HorizontalPagerIndicator].
 */
@Immutable
private data class DefaultLibraryPadding(
    override val outerPadding: PaddingValues,
    override val innerPadding: PaddingValues,
    override val namePadding: PaddingValues,
    override val actionIconPadding: PaddingValues,
    override val developerPadding: PaddingValues,
    override val bodyPadding: PaddingValues,
    override val footerPadding: PaddingValues,
    override val pageIndicatorPadding: PaddingValues,
) : LibraryPadding

/**
 *  Represents various values used by [AboutLibrary].
 *
 *  @property shape Shape of surface.
 *  @property borderWidth Thickness of border in [Dp].
 *  @property contentSpacedBy Vertical spacing between Composables in [Dp].
 *  @property dividerThickness Thickness of Dividers in [Dp].
 *  @property actionIcon Icon used for action button.
 *  @property actionIconSize Size of action Icon.
 *  @property pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
 */
@Stable
interface LibraryExtras {
    val shape: Shape
    val borderWidth: Dp
    val contentSpacedBy: Dp
    val dividerThickness: Dp
    val actionIcon: Painter
    val actionIconSize: Dp
    val pagerIndicatorExtras: PagerIndicatorExtras
}

/**
 *  Default [LibraryExtras].
 *
 *  @param shape Shape of surface.
 *  @param borderWidth Thickness of border in [Dp].
 *  @param contentSpacedBy Vertical spacing between Composables in [Dp].
 *  @param dividerThickness Thickness of Dividers in [Dp].
 *  @param actionIcon Icon used for action button.
 *  @param actionIconSize Size of action Icon.
 *  @param pagerIndicatorExtras Values used by [HorizontalPagerIndicator].
 */
@Immutable
private data class DefaultLibraryExtras(
    override val shape: Shape,
    override val borderWidth: Dp,
    override val contentSpacedBy: Dp,
    override val dividerThickness: Dp,
    override val actionIcon: Painter,
    override val actionIconSize: Dp,
    override val pagerIndicatorExtras: PagerIndicatorExtras,
) : LibraryExtras

/**
 *  Represents text styles used by [AboutLibrary].
 *
 *  @property nameStyle Text style used by name Text.
 *  @property developerStyle Text style used by developer Text.
 *  @property bodyStyle Text style used by body Text.
 *  @property footerStyle Text style used by body Text.
 */
@Stable
interface LibraryTextStyles {
    val nameStyle: TextStyle
    val developerStyle: TextStyle
    val bodyStyle: TextStyle
    val footerStyle: TextStyle
}

/**
 *  Default [LibraryTextStyles].
 *
 *  @param nameStyle Text style used by name Text.
 *  @param developerStyle Text style used by developer Text.
 *  @param bodyStyle Text style used by body Text.
 *  @param footerStyle Text style used by body Text.
 */
@Immutable
private data class DefaultLibraryTextStyles(
    override val nameStyle: TextStyle,
    override val developerStyle: TextStyle,
    override val bodyStyle: TextStyle,
    override val footerStyle: TextStyle,
) : LibraryTextStyles