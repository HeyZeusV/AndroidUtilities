@file:OptIn(ExperimentalSharedTransitionApi::class)

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.about.AboutColors
import com.heyzeusv.androidutilities.compose.ui.about.AboutTextStyles
import com.heyzeusv.androidutilities.compose.ui.library.LibrarySharedContentPrefix.*
import com.heyzeusv.androidutilities.compose.ui.pageindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.formatContent
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.entity.Library

@Composable
internal fun LibraryScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    backOnClick: () -> Unit,
    library: Library,
    initialPagerPage: Int,
    colors: LibraryColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: LibraryTextStyles,
) {
    Column {
        LibraryScreen(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            backOnClick = backOnClick,
            library = library,
            initialPagerPage = initialPagerPage,
            colors = colors,
            padding = padding,
            dimensions = dimensions,
            textStyles = textStyles,
        )
    }
}

@Composable
internal fun ColumnScope.LibraryScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    backOnClick: () -> Unit,
    library: Library,
    initialPagerPage: Int,
    colors: LibraryColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: LibraryTextStyles,
) {
    val pagerState = rememberPagerState(initialPage = initialPagerPage) { 2 }

    LibraryDetails(
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        modifier = Modifier.fillMaxSize(),
        pagerModifier = Modifier.weight(1f),
        bodyModifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        isFullscreen = true,
        backOnClick = backOnClick,
        library = library,
        pagerState = pagerState,
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
    libraryOnClick: (String, String, Int) -> Unit,
    bodyMaxLines: Int,
    colors: AboutColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: AboutTextStyles,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.contentSpacing)
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
                val pagerState = rememberPagerState(pageCount = { 2 })

                LibraryDetails(
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    modifier = Modifier.clickable {
                        libraryOnClick(info.id, library.uniqueId, pagerState.currentPage)
                    },
                    isFullscreen = false,
                    library = library,
                    pagerState = pagerState,
                    bodyMaxLines = bodyMaxLines,
                    colors = colors.libraryItemColors,
                    padding = padding,
                    dimensions = dimensions,
                    textStyles = textStyles.libraryItemStyles,
                )
            }
        }
    }
}

@Composable
internal fun LibraryDetails(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier,
    pagerModifier: Modifier = Modifier,
    bodyModifier: Modifier = Modifier,
    isFullscreen: Boolean,
    backOnClick: () -> Unit = { },
    library: Library,
    pagerState: PagerState,
    bodyMaxLines: Int,
    colors: LibraryColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: LibraryTextStyles,
) {
    // TODO: Maybe add string if blank developers
    val developers = library.developers.map { it.name }.joinToString(separator = ", ")
    val description = library.description.ifNullOrBlank(sRes(R.string.library_description_empty))
    val version = library.artifactVersion.ifNullOrBlank(sRes(R.string.library_version_empty))
    val license = library.licenses.firstOrNull()
    val licenseContent = license?.licenseContent.ifNullOrBlank(sRes(R.string.library_license_empty))
    val licenseName = license?.name.ifNullOrBlank(sRes(R.string.library_license_empty))

    with(sharedTransitionScope) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .sharedElement(
                    state = librarySCS(SURFACE, library.uniqueId),
                    animatedVisibilityScope = animatedContentScope,
                ),
            shape = dimensions.shape,
            color = colors.backgroundColor,
            border = BorderStroke(width = dimensions.borderWidth, color = colors.borderColor),
        ) {
            Column(
                modifier = Modifier.padding(padding.contentPadding),
                verticalArrangement = Arrangement.spacedBy(dimensions.contentSpacing)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFullscreen) {
                        IconButton(onClick = { backOnClick() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null,
                                tint = colors.contentColor
                            )
                        }
                    }
                    Text(
                        text = library.name,
                        modifier = Modifier
                            .padding(padding.namePadding)
                            .fillMaxWidth()
                            .basicMarquee()
                            .sharedElement(
                                state = librarySCS(NAME, library.uniqueId),
                                animatedVisibilityScope = animatedContentScope
                            ),
                        maxLines = 1,
                        style = textStyles.nameStyle,
                    )
                }
                Text(
                    text = developers,
                    modifier = Modifier
                        .padding(padding.developerPadding)
                        .fillMaxWidth()
                        .basicMarquee()
                        .sharedElement(
                            state = librarySCS(DEVELOPER, library.uniqueId),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    maxLines = 1,
                    style = textStyles.developerStyle,
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = pagerModifier,
                ) { page ->
                    val body: String = if (page == 0) description else licenseContent
                    val footer: String = if (page == 0) version else licenseName
                    LibraryInfo(
                        animatedContentScope = animatedContentScope,
                        sharedContentKey = "${library.uniqueId}-$page",
                        modifier = bodyModifier,
                        body = body.formatContent(),
                        bodyMaxLines = bodyMaxLines,
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
                        .sharedElement(
                            state = librarySCS(PAGER_INDICATOR, library.uniqueId),
                            animatedVisibilityScope = animatedContentScope
                        ),
                )
            }
        }
    }
}

@Composable
internal fun SharedTransitionScope.LibraryInfo(
    animatedContentScope: AnimatedContentScope,
    sharedContentKey: String,
    modifier: Modifier,
    body: String,
    bodyMaxLines: Int,
    footer: String,
    colors: LibraryColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: LibraryTextStyles,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensions.contentSpacing)) {
        HorizontalDivider(
            modifier = Modifier.sharedElement(
                state = librarySCS(TOP_DIVIDER, sharedContentKey),
                animatedVisibilityScope = animatedContentScope,
            ),
            thickness = dimensions.dividerThickness,
            color = colors.dividerColor,
        )
        Text(
            text = body,
            modifier = modifier
                .padding(padding.bodyPadding)
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = librarySCS(BODY, sharedContentKey),
                    animatedVisibilityScope = animatedContentScope,
                ),
            color = colors.contentColor,
            maxLines = bodyMaxLines,
            style = textStyles.bodyStyle,
        )
        HorizontalDivider(
            modifier = Modifier.sharedElement(
                state = librarySCS(BOTTOM_DIVIDER, sharedContentKey),
                animatedVisibilityScope = animatedContentScope,
            ),
            thickness = dimensions.dividerThickness,
            color = colors.dividerColor,
        )
        Text(
            text = footer,
            modifier = Modifier
                .padding(padding.footerPadding)
                .align(Alignment.End)
                .sharedElement(
                    state = librarySCS(FOOTER, sharedContentKey),
                    animatedVisibilityScope = animatedContentScope,
                ),
            color = colors.contentColor,
            style = textStyles.footerStyle,
        )
    }
}