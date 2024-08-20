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
import androidx.compose.foundation.layout.height
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
internal fun SharedTransitionScope.LibraryScreen(
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

context(SharedTransitionScope)
@Composable
internal fun ColumnScope.LibraryScreen(
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
        animatedContentScope = animatedContentScope,
        modifier = Modifier.fillMaxSize(),
        pagerModifier = Modifier.weight(1f),
        bodyModifier = Modifier
            .weight(1f)
            .padding(padding.bodyPadding)
            .verticalScroll(rememberScrollState()),
        isFullscreen = true,
        backOnClick = backOnClick,
        library = library,
        pagerState = pagerState,
        colors = colors,
        padding = padding,
        dimensions = dimensions,
        textStyles = textStyles,
    )
}

@Composable
internal fun SharedTransitionScope.LibraryList(
    animatedContentScope: AnimatedContentScope,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (String, String, Int) -> Unit,
    colors: AboutColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: AboutTextStyles,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(LibraryDefaults.ContentSpacing)
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
                    animatedContentScope = animatedContentScope,
                    modifier = Modifier.clickable {
                        libraryOnClick(info.id, library.uniqueId, pagerState.currentPage)
                    },
                    bodyModifier = Modifier
                        .padding(padding.bodyPadding)
                        .height(LibraryDefaults.ItemBodyHeight),
                    isFullscreen = false,
                    library = library,
                    pagerState = pagerState,
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
internal fun SharedTransitionScope.LibraryDetails(
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier,
    pagerModifier: Modifier = Modifier,
    bodyModifier: Modifier,
    isFullscreen: Boolean,
    backOnClick: () -> Unit = { },
    library: Library,
    pagerState: PagerState,
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
                            animatedVisibilityScope = animatedContentScope,
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
                    footer = footer,
                    colors = colors,
                    padding = padding,
                    dimensions = dimensions,
                    textStyles = textStyles,
                )
            }
            Surface(
                modifier = Modifier
                    .padding(bottom = dimensions.borderWidth)
                    .fillMaxWidth()
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
                color = colors.backgroundColor,
            ) {
                Column {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = 2,
                        modifier = Modifier
                            .padding(padding.pageIndicatorPadding)
                            .align(Alignment.CenterHorizontally)
                            .sharedElement(
                                state = librarySCS(PAGER_INDICATOR, library.uniqueId),
                                animatedVisibilityScope = animatedContentScope,
                                zIndexInOverlay = 2f,
                            ),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.sharedElement(
                    state = librarySCS(prefix = BOTTOM_DIVIDER, key = library.uniqueId),
                    animatedVisibilityScope = animatedContentScope,
                    zIndexInOverlay = 3f,
                ),
                thickness = dimensions.borderWidth,
                color = colors.borderColor,
            )
        }
    }
}

@Composable
internal fun SharedTransitionScope.LibraryInfo(
    animatedContentScope: AnimatedContentScope,
    sharedContentKey: String,
    modifier: Modifier,
    body: String,
    footer: String,
    colors: LibraryColors,
    padding: LibraryPadding,
    dimensions: LibraryDimensions,
    textStyles: LibraryTextStyles,
) {
    Column {
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
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = librarySCS(BODY, sharedContentKey),
                    animatedVisibilityScope = animatedContentScope,
                ),
            color = colors.contentColor,

            style = textStyles.bodyStyle,
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f),
            color = colors.backgroundColor
        ) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.sharedElement(
                        state = librarySCS(MIDDLE_DIVIDER, sharedContentKey),
                        animatedVisibilityScope = animatedContentScope,
                        zIndexInOverlay = 2f,
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
                            zIndexInOverlay = 2f,
                        ),
                    color = colors.contentColor,
                    style = textStyles.footerStyle,
                )
            }
        }
    }
}