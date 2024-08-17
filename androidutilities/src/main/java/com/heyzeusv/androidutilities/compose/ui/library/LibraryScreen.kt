@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.about.AboutColors
import com.heyzeusv.androidutilities.compose.ui.about.AboutDefaults
import com.heyzeusv.androidutilities.compose.ui.about.AboutDimensions
import com.heyzeusv.androidutilities.compose.ui.about.AboutPadding
import com.heyzeusv.androidutilities.compose.ui.about.AboutTextStyles
import com.heyzeusv.androidutilities.compose.ui.pageindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.entity.Library

@Composable
internal fun LibraryScreen(
    modifier: Modifier,
    library: Library,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    Column {
        LibraryScreen(
            modifier = modifier,
            library = library,
            colors = colors,
            padding = padding,
            dimensions = dimensions,
            textStyles = textStyles,
        )
    }
}

@Composable
internal fun ColumnScope.LibraryScreen(
    modifier: Modifier,
    library: Library,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    LibraryDetails(
        modifier = modifier.fillMaxSize(),
        pagerModifier = Modifier.weight(1f),
        bodyModifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        isFullscreen = true,
        library = library,
        bodyMaxLines = Int.MAX_VALUE,
        colors = colors,
        padding = padding,
        dimensions = dimensions,
        textStyles = textStyles,
    )
}

@Composable
internal fun LibraryList(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (String, String) -> Unit,
    bodyMaxLines: Int,
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.libraryItemSpacing)
    ) {
        libraries.forEach { (info, libs) ->
            item {
                Text(
                    text = sRes(info.headerId),
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.libraryHeaderColor,
                    style = textStyles.libraryHeaderStyle,
                )
            }
            items(
                items = libs,
                key = { it.uniqueId }
            ) { library ->
                with(sharedTransitionScope) {
                    val sharedKey = "library-${library.uniqueId}"
                    LibraryDetails(
                        modifier = Modifier
                            .clickable { libraryOnClick(info.id, library.uniqueId) }
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(sharedKey),
                                animatedVisibilityScope = animatedContentScope
                            ),
                        isFullscreen = false,
                        library = library,
                        bodyMaxLines = bodyMaxLines,
                        colors = colors.libraryColors,
                        padding = padding.libraryPadding,
                        dimensions = dimensions.libraryDimensions,
                        textStyles = textStyles.libraryStyles,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LibraryDetails(
    modifier: Modifier,
    pagerModifier: Modifier = Modifier,
    bodyModifier: Modifier = Modifier,
    isFullscreen: Boolean,
    library: Library,
    bodyMaxLines: Int,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val pagerState = rememberPagerState(pageCount = { if (isFullscreen) 2 else 1 })
    val developers = library.developers.map { it.name }.joinToString(separator = ", ")
    val description = library.description.ifNullOrBlank(sRes(R.string.library_description_empty))
    val version = library.artifactVersion.ifNullOrBlank(sRes(R.string.library_version_empty))
    val license = library.licenses.firstOrNull()
    val licenseContent = license?.licenseContent.ifNullOrBlank(sRes(R.string.library_license_empty))
    val licenseName = license?.name.ifNullOrBlank(sRes(R.string.library_license_empty))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = dimensions.shape,
        color = colors.backgroundColor,
        border = BorderStroke(width = dimensions.borderWidth, color = colors.borderColor),
    ) {
        Column(
            modifier = Modifier.padding(padding.contentPadding),
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
            HorizontalPager(state = pagerState, modifier = pagerModifier) { page ->
                val body: String = if (page == 0) description else licenseContent
                val footer: String = if (page == 0) version else licenseName
                LibraryInfo(
                    modifier = bodyModifier,
                    body = body,
                    bodyMaxLines = bodyMaxLines,
                    footer = footer,
                    colors = colors,
                    padding = padding,
                    dimensions = dimensions,
                    textStyles = textStyles,
                )
            }
            if (isFullscreen) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = 2,
                    modifier = Modifier
                        .padding(padding.pageIndicatorPadding)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
internal fun LibraryInfo(
    modifier: Modifier,
    body: String,
    bodyMaxLines: Int,
    footer: String,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
        HorizontalDivider(
            thickness = dimensions.dividerThickness,
            color = colors.dividerColor,
        )
        Text(
            text = body,
            modifier = modifier
                .padding(padding.bodyPadding)
                .fillMaxWidth(),
            color = colors.contentColor,
            maxLines = bodyMaxLines,
            style = textStyles.bodyStyle,
        )
        HorizontalDivider(
            thickness = dimensions.dividerThickness,
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