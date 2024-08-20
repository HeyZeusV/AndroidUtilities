package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LibraryDefaults {
    private val ItemBorderWidth = 2.dp
    private val DividerThickness = 2.dp
    private val ItemCornerRadius = 12.dp
    private val ContentPadding = 12.dp
    val ContentSpacing = 8.dp
    private val HalfContentSpacing = 4.dp
    val ItemBodyHeight = 75.dp
    private val ScreenBorderWidth = 0.dp
    private val ScreenCornerRadius = 0.dp

    private val ContentPV = PaddingValues(start = ContentPadding, end = ContentPadding)
    private val NamePV = PaddingValues(top = ContentPadding, bottom = HalfContentSpacing)
    private val DeveloperPV = PaddingValues(vertical = HalfContentSpacing)
    private val BodyPV = PaddingValues(vertical = HalfContentSpacing)
    private val FooterPV = PaddingValues(vertical = HalfContentSpacing)
    private val PageIndicatorPV = PaddingValues(top = HalfContentSpacing, bottom = ContentPadding)

    @Composable
    fun libraryItemColors(
        borderColor: Color = MaterialTheme.colorScheme.onSurface,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        dividerColor: Color = MaterialTheme.colorScheme.onSurface,
    ): LibraryColors = DefaultLibraryColors(
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        dividerColor = dividerColor
    )

    @Composable
    fun libraryScreenColors(
        borderColor: Color = Color.Transparent,
        backgroundColor: Color = MaterialTheme.colorScheme.surface,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        dividerColor: Color = MaterialTheme.colorScheme.onSurface,
    ): LibraryColors = DefaultLibraryColors(
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        dividerColor = dividerColor
    )

    @Composable
    fun libraryPadding(
        contentPadding: PaddingValues = ContentPV,
        namePadding: PaddingValues = NamePV,
        developerPadding: PaddingValues = DeveloperPV,
        bodyPadding: PaddingValues = BodyPV,
        footerPadding: PaddingValues = FooterPV,
        pageIndicatorPadding: PaddingValues = PageIndicatorPV,
    ): LibraryPadding = DefaultLibraryPadding(
        contentPadding = contentPadding,
        namePadding = namePadding,
        developerPadding = developerPadding,
        bodyPadding = bodyPadding,
        footerPadding = footerPadding,
        pageIndicatorPadding = pageIndicatorPadding,
    )

    @Composable
    fun libraryItemDimensions(
        shape: Shape = RoundedCornerShape(ItemCornerRadius),
        borderWidth: Dp = ItemBorderWidth,
        dividerThickness: Dp = DividerThickness,
    ): LibraryDimensions = DefaultLibraryDimensions(
        shape = shape,
        borderWidth = borderWidth,
        dividerThickness = dividerThickness,
    )

    @Composable
    fun libraryScreenDimensions(
        shape: Shape = RoundedCornerShape(ScreenCornerRadius),
        borderWidth: Dp = ScreenBorderWidth,
        dividerThickness: Dp = DividerThickness,
    ): LibraryDimensions = DefaultLibraryDimensions(
        shape = shape,
        borderWidth = borderWidth,
        dividerThickness = dividerThickness,
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
}

@Immutable
private data class DefaultLibraryColors(
    override val borderColor: Color,
    override val backgroundColor: Color,
    override val contentColor: Color,
    override val dividerColor: Color
) : LibraryColors

@Stable
interface LibraryPadding {
    val contentPadding: PaddingValues
    val namePadding: PaddingValues
    val developerPadding: PaddingValues
    val bodyPadding: PaddingValues
    val footerPadding: PaddingValues
    val pageIndicatorPadding: PaddingValues
}

@Immutable
private data class DefaultLibraryPadding(
    override val contentPadding: PaddingValues,
    override val namePadding: PaddingValues,
    override val developerPadding: PaddingValues,
    override val bodyPadding: PaddingValues,
    override val footerPadding: PaddingValues,
    override val pageIndicatorPadding: PaddingValues,
) : LibraryPadding

@Stable
interface LibraryDimensions {
    val shape: Shape
    val borderWidth: Dp
    val dividerThickness: Dp
}

@Immutable
private data class DefaultLibraryDimensions(
    override val shape: Shape,
    override val borderWidth: Dp,
    override val dividerThickness: Dp,
) : LibraryDimensions

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