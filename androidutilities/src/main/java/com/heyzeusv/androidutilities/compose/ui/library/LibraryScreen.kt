@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyzeusv.androidutilities.R
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
    colors: LibraryColors,
    padding: LibraryPadding,
    extras: LibraryExtras,
    textStyles: LibraryTextStyles,
) {
    Column(modifier = Modifier.padding(padding.outerPadding)) {
        LibraryScreen(
            animatedContentScope = animatedContentScope,
            backOnClick = backOnClick,
            library = library,
            colors = colors,
            padding = padding,
            extras = extras,
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
    colors: LibraryColors,
    padding: LibraryPadding,
    extras: LibraryExtras,
    textStyles: LibraryTextStyles,
) {
    LibraryDetails(
        animatedContentScope = animatedContentScope,
        modifier = Modifier.fillMaxSize(),
        isFullscreen = true,
        actionOnClick = backOnClick,
        library = library,
        colors = colors,
        padding = padding,
        extras = extras,
        textStyles = textStyles,
    )
}

@Composable
internal fun SharedTransitionScope.LibraryDetails(
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean,
    actionOnClick: () -> Unit,
    library: Library,
    colors: LibraryColors,
    padding: LibraryPadding,
    extras: LibraryExtras,
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
        shape = extras.shape,
        color = colors.backgroundColor,
        border = BorderStroke(width = extras.borderWidth, color = colors.borderColor),
    ) {
        Column(
            modifier = Modifier.padding(padding.innerPadding),
            verticalArrangement = Arrangement.spacedBy(extras.contentSpacedBy),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = library.name,
                    modifier = Modifier
                        .padding(padding.namePadding)
                        .weight(1f)
                        .sharedElement(
                            state = librarySCS(NAME, library.uniqueId),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    style = textStyles.nameStyle,
                )
                Icon(
                    painter = extras.actionIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(padding.actionIconPadding)
                        .size(extras.actionIconSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(
                                bounded = false,
                                radius = LibraryDefaults.ActionIconRippleRadius,
                            ),
                            onClick = actionOnClick,
                        ),
                    tint = colors.contentColor,
                )
            }
            Text(
                text = developers,
                modifier = Modifier
                    .padding(padding.developerPadding)
                    .fillMaxWidth()
                    .sharedElement(
                        state = librarySCS(DEVELOPER, library.uniqueId),
                        animatedVisibilityScope = animatedContentScope,
                    ),
                style = textStyles.developerStyle,
            )
            if (isFullscreen) {
                val pagerState = rememberPagerState(pageCount = { 2 })
                val scaleSpring = spring<Float>(stiffness = Spring.StiffnessMedium)

                with(animatedContentScope) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .animateEnterExit(
                                enter = scaleIn(scaleSpring),
                                exit = scaleOut(scaleSpring),
                            ),
                        verticalArrangement = Arrangement.spacedBy(extras.contentSpacedBy),
                    ) {
                        HorizontalDivider(
                            thickness = extras.dividerThickness,
                            color = colors.dividerColor,
                        )
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f),
                        ) { page ->
                            val body: String = if (page == 0) description else licenseContent
                            Text(
                                text = body.formatContent(),
                                modifier = Modifier
                                    .padding(padding.bodyPadding)
                                    .fillMaxSize()
                                    .skipToLookaheadSize()
                                    .verticalScroll(rememberScrollState()),
                                color = colors.contentColor,
                                style = textStyles.bodyStyle,
                            )
                        }

                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            pageCount = 2,
                            modifier = Modifier
                                .padding(padding.pageIndicatorPadding)
                                .align(Alignment.CenterHorizontally),
                            activeColor = colors.pagerIndicatorColors.activeColor,
                            inactiveColor = colors.pagerIndicatorColors.inactiveColor,
                            indicatorWidth = extras.pagerIndicatorExtras.indicatorWidth,
                            indicatorHeight = extras.pagerIndicatorExtras.indicatorHeight,
                            indicatorSpacing = extras.pagerIndicatorExtras.indicatorSpacing,
                            indicatorShape = extras.pagerIndicatorExtras.indicatorShape,
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.sharedElement(
                    state = librarySCS(BOTTOM_DIVIDER, library.uniqueId),
                    animatedVisibilityScope = animatedContentScope,
                    zIndexInOverlay = 2f,
                ),
                thickness = extras.dividerThickness,
                color = colors.dividerColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        state = librarySCS(FOOTER, library.uniqueId),
                        animatedVisibilityScope = animatedContentScope,
                        zIndexInOverlay = 2f,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = licenseName,
                    modifier = Modifier.padding(padding.footerPadding),
                    color = colors.contentColor,
                    style = textStyles.footerStyle,
                )
                Text(
                    text = version,
                    modifier = Modifier.padding(padding.footerPadding),
                    color = colors.contentColor,
                    style = textStyles.footerStyle,
                )
            }
        }
    }
}