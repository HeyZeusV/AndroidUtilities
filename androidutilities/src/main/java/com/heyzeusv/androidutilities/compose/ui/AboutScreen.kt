package com.heyzeusv.androidutilities.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ANDROID = "androidx"
private const val JETBRAINS = "org.jetbrains"
private const val GOOGLE = "com.google"
private val firstPartyIds = listOf(ANDROID, JETBRAINS, GOOGLE)

@Composable
fun AboutScreen(
    separateByParty: Boolean = true,
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Text(text = "Third-Party Libraries") }
        items(libraries.second) { LibraryItem(library = it) }
        if (separateByParty) {
            item { Text(text = "First-Party Libraries") }
            items(libraries.first) { LibraryItem(library = it) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItem(
    library: Library,
    shape: Shape = LibraryDefaults.Shape,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    contentPadding: PaddingValues = LibraryDefaults.ContentPadding,
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val developers = library.developers.map { it.name }.joinToString(separator = ", ")
    val description = library.description.ifNullOrBlank(sRes(R.string.library_description_empty))
    val version = library.artifactVersion.ifNullOrBlank(sRes(R.string.library_version_empty))
    val license = library.licenses.firstOrNull()
    val licenseContent = license?.licenseContent.ifNullOrBlank(sRes(R.string.library_license_empty))
    val licenseName = license?.name.ifNullOrBlank(sRes(R.string.library_license_empty))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = colors.backgroundColor,
        border = BorderStroke(width = dimensions.borderWidth, color = colors.borderColor),
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
        ) {
            Text(
                text = library.name,
                modifier = Modifier
                    .padding(padding.namePadding)
                    .fillMaxWidth()
                    .basicMarquee(),
                maxLines = 1,
                style = textStyles.nameStyle,
            )
            Text(
                text = developers,
                modifier = Modifier
                    .padding(padding.developerPadding)
                    .fillMaxWidth()
                    .basicMarquee(),
                maxLines = 1,
                style = textStyles.developerStyle
            )
            HorizontalPager(state = pagerState) { page ->
                val body: String = if (page == 0) description else licenseContent
                val footer: String = if (page == 0) version else licenseName
                LibraryInfo(
                    body = body,
                    footer = footer,
                    colors = colors,
                    padding = padding,
                    dimensions = dimensions,
                    textStyles = textStyles,
                )
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = 2,
                modifier = Modifier
                    .padding(padding.pageIndicatorPadding)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
internal fun LibraryInfo(
    body: String,
    footer: String,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
        HorizontalDivider(
            thickness = dimensions.dividerWidth,
            color = colors.dividerColor,
        )
        Text(
            text = body,
            modifier = Modifier
                .padding(padding.bodyPadding)
                .fillMaxWidth(),
            color = colors.contentColor,
            maxLines = 5,
            minLines = 5,
            style = textStyles.bodyStyle,
        )
        HorizontalDivider(
            thickness = dimensions.dividerWidth,
            color = colors.dividerColor,
        )
        Text(
            text = footer,
            modifier = Modifier
                .padding(padding.footerPadding)
                .align(Alignment.End),
            color = colors.contentColor,
            style = textStyles.footerStyle,
        )
    }
}

@Composable
private fun produceLibraryState(separateByParty: Boolean): State<Pair<List<Library>, List<Library>>> {
    val context = LocalContext.current

    return produceState(Pair(listOf(), listOf())) {
        value = withContext(Dispatchers.IO) {
            val libs = Libs.Builder().withContext(context).build()
            if (separateByParty) {
                libs.libraries.partition { library ->
                    firstPartyIds.any { library.uniqueId.contains(it) }
                }
            } else {
                Pair(libs.libraries, listOf())
            }
        }
        
    }
}

object LibraryDefaults {
    private val BorderWidth = 2.dp
    private val DividerWidth = 2.dp
    private val CornerRadius = 12.dp
    private val ItemPadding = 12.dp
    private val ItemSpacing = 8.dp

    val Shape = RoundedCornerShape(CornerRadius)
    val ContentPadding = PaddingValues(ItemPadding)

    @Composable
    fun libraryColors(
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
    fun libraryPadding(
        namePadding: PaddingValues = PaddingValues(),
        developerPadding: PaddingValues = PaddingValues(),
        bodyPadding: PaddingValues = PaddingValues(),
        footerPadding: PaddingValues = PaddingValues(),
        pageIndicatorPadding: PaddingValues = PaddingValues(),
    ): LibraryPadding = DefaultLibraryPadding(
        namePadding = namePadding,
        developerPadding = developerPadding,
        bodyPadding = bodyPadding,
        footerPadding = footerPadding,
        pageIndicatorPadding = pageIndicatorPadding,
    )

    @Composable
    fun libraryDimensions(
        borderWidth: Dp = BorderWidth,
        itemSpacing: Dp = ItemSpacing,
        dividerWidth: Dp = DividerWidth,
    ): LibraryDimensions = DefaultLibraryDimensions(
        borderWidth = borderWidth,
        itemSpacing = itemSpacing,
        dividerWidth = dividerWidth,
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
    val namePadding: PaddingValues
    val developerPadding: PaddingValues
    val bodyPadding: PaddingValues
    val footerPadding: PaddingValues
    val pageIndicatorPadding: PaddingValues
}

@Immutable
private data class DefaultLibraryPadding(
    override val namePadding: PaddingValues,
    override val developerPadding: PaddingValues,
    override val bodyPadding: PaddingValues,
    override val footerPadding: PaddingValues,
    override val pageIndicatorPadding: PaddingValues,
) : LibraryPadding

@Stable
interface LibraryDimensions {
    val borderWidth: Dp
    val itemSpacing: Dp
    val dividerWidth: Dp
}

@Immutable
private data class DefaultLibraryDimensions(
    override val borderWidth: Dp,
    override val itemSpacing: Dp,
    override val dividerWidth: Dp,
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