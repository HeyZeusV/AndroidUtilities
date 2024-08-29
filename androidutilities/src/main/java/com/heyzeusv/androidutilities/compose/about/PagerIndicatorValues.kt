package com.heyzeusv.androidutilities.compose.about

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.compose.pagerindicator.HorizontalPagerIndicator

/**
 *  Contains the default values used by [HorizontalPagerIndicator] on [AboutScreen].
 */
object PagerIndicatorDefaults {

    /**
     *  Creates a [PagerIndicatorColors] that represents the colors used by
     *  [HorizontalPagerIndicator].
     *
     *  @param activeColor Color of indicator of currently displayed page.
     *  @param inactiveColor Color of indicator of page when not displayed.
     */
    @Composable
    fun pagerIndicatorColors(
        activeColor: Color = LocalContentColor.current.copy(alpha = 0.9f),
        inactiveColor: Color = activeColor.copy(alpha = 0.38f),
    ): PagerIndicatorColors = DefaultPagerIndicatorColors(
        activeColor = activeColor,
        inactiveColor = inactiveColor
    )

    /**
     *  Creates a [PagerIndicatorExtras] that represents the dimensions and the shape used by
     *  [HorizontalPagerIndicator].
     *
     *  @param indicatorWidth The width of each individual indicator in [Dp].
     *  @param indicatorHeight The height of each individual indicator in [Dp].
     *  @param indicatorSpacing The space between each indicator in [Dp].
     *  @param indicatorShape The [Shape] of each indicator.
     */
    @Composable
    fun pagerIndicatorExtras(
        indicatorWidth: Dp = 8.dp,
        indicatorHeight: Dp = indicatorWidth,
        indicatorSpacing: Dp = indicatorWidth,
        indicatorShape: Shape = CircleShape
    ): PagerIndicatorExtras = DefaultPagerIndicatorExtras(
        indicatorWidth = indicatorWidth,
        indicatorHeight = indicatorHeight,
        indicatorSpacing = indicatorSpacing,
        indicatorShape = indicatorShape,
    )
}

/**
 *  Represents the colors used by [HorizontalPagerIndicator].
 *
 *  @property activeColor Color of indicator of currently displayed page.
 *  @property inactiveColor Color of indicator of page when not displayed.
 */
@Stable
interface PagerIndicatorColors {
    val activeColor: Color
    val inactiveColor: Color
}

/**
 *  Default [PagerIndicatorColors].
 *
 *  @param activeColor Color of indicator of currently displayed page.
 *  @param inactiveColor Color of indicator of page when not displayed.
 */
@Immutable
private data class DefaultPagerIndicatorColors(
    override val activeColor: Color,
    override val inactiveColor: Color,
) : PagerIndicatorColors

/**
 *  Represents dimensions and shape used by [HorizontalPagerIndicator].
 *
 *  @property indicatorWidth The width of each individual indicator in [Dp].
 *  @property indicatorHeight The height of each individual indicator in [Dp].
 *  @property indicatorSpacing The space between each indicator in [Dp].
 *  @property indicatorShape The [Shape] of each indicator.
 */
@Stable
interface PagerIndicatorExtras {
    val indicatorWidth: Dp
    val indicatorHeight: Dp
    val indicatorSpacing: Dp
    val indicatorShape: Shape
}

/**
 *  Default [PagerIndicatorExtras].
 *
 *  @param indicatorWidth The width of each individual indicator in [Dp].
 *  @param indicatorHeight The height of each individual indicator in [Dp].
 *  @param indicatorSpacing The space between each indicator in [Dp].
 *  @param indicatorShape The [Shape] of each indicator.
 */
@Immutable
private data class DefaultPagerIndicatorExtras(
    override val indicatorWidth: Dp,
    override val indicatorHeight: Dp,
    override val indicatorSpacing: Dp,
    override val indicatorShape: Shape,
) : PagerIndicatorExtras