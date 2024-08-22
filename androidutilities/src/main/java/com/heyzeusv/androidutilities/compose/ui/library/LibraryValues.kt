package com.heyzeusv.androidutilities.compose.ui.library

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
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorColors
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorDefaults
import com.heyzeusv.androidutilities.compose.ui.pageindicator.PagerIndicatorExtras
import com.heyzeusv.androidutilities.compose.util.pRes

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

@Stable
interface LibraryColors {
    val borderColor: Color
    val backgroundColor: Color
    val contentColor: Color
    val dividerColor: Color
    val pagerIndicatorColors: PagerIndicatorColors
}

@Immutable
private data class DefaultLibraryColors(
    override val borderColor: Color,
    override val backgroundColor: Color,
    override val contentColor: Color,
    override val dividerColor: Color,
    override val pagerIndicatorColors: PagerIndicatorColors,
) : LibraryColors

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

@Stable
interface LibraryTextStyles {
    val nameStyle: TextStyle
    val developerStyle: TextStyle
    val bodyStyle: TextStyle
    val footerStyle: TextStyle
}

@Immutable
private data class DefaultLibraryTextStyles(
    override val nameStyle: TextStyle,
    override val developerStyle: TextStyle,
    override val bodyStyle: TextStyle,
    override val footerStyle: TextStyle,
) : LibraryTextStyles