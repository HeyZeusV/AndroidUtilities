package com.heyzeusv.androidutilities.compose.ui.pageindicator

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PagerIndicatorDefaults {

    @Composable
    fun pagerIndicatorColors(
        activeColor: Color = LocalContentColor.current.copy(alpha = 0.9f),
        inactiveColor: Color = activeColor.copy(alpha = 0.38f),
    ): PagerIndicatorColors = DefaultPagerIndicatorColors(
        activeColor = activeColor,
        inactiveColor = inactiveColor
    )

    @Composable
    fun pagerIndicatorDimensions(
        indicatorWidth: Dp = 8.dp,
        indicatorHeight: Dp = indicatorWidth,
        indicatorSpacing: Dp = indicatorWidth,
        indicatorShape: Shape = CircleShape
    ): PagerIndicatorDimensions = DefaultPagerIndicatorDimensions(
        indicatorWidth = indicatorWidth,
        indicatorHeight = indicatorHeight,
        indicatorSpacing = indicatorSpacing,
        indicatorShape = indicatorShape,
    )
}

@Stable
interface PagerIndicatorColors {
    val activeColor: Color
    val inactiveColor: Color
}

@Immutable
private data class DefaultPagerIndicatorColors(
    override val activeColor: Color,
    override val inactiveColor: Color,
) : PagerIndicatorColors

@Stable
interface PagerIndicatorDimensions {
    val indicatorWidth: Dp
    val indicatorHeight: Dp
    val indicatorSpacing: Dp
    val indicatorShape: Shape
}

@Immutable
private data class DefaultPagerIndicatorDimensions(
    override val indicatorWidth: Dp,
    override val indicatorHeight: Dp,
    override val indicatorSpacing: Dp,
    override val indicatorShape: Shape,
) : PagerIndicatorDimensions