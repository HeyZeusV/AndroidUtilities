@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.heyzeusv.androidutilities.compose.ui.library.LibraryDetails
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPartyInfo
import com.heyzeusv.androidutilities.compose.ui.pageindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.entity.Library

context(AnimatedContentScope)
@Composable
internal fun SharedTransitionScope.AboutScreen(
    animatedContentScope: AnimatedContentScope,
    icon: @Composable () -> Unit = { },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (Library) -> Unit,
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    extras: AboutExtras = AboutDefaults.aboutExtras(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppInfo(
            icon = icon,
            title = title,
            version = version,
            info = info,
            colors = colors,
            padding = padding,
            extras = extras,
            textStyles = textStyles,
        )
        LibraryList(
            animatedContentScope = animatedContentScope,
            libraries = libraries,
            libraryOnClick = libraryOnClick,
            colors = colors,
            padding = padding,
            extras = extras,
            textStyles = textStyles,
        )
    }
}

context(AnimatedContentScope)
@Composable
internal fun SharedTransitionScope.AppInfo(
    icon: @Composable () -> Unit = { },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    extras: AboutExtras = AboutDefaults.aboutExtras(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    val pagerState = rememberPagerState(pageCount = { info.size })

    Surface(
        modifier = Modifier
            .padding(padding.appInfoPadding)
            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 10f)
            .animateEnterExit(
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ),
        color = colors.backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(extras.appInfoItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            Text(
                text = title,
                modifier = Modifier.padding(padding.titlePadding),
                color = colors.titleColor,
                style = textStyles.titleStyle,
            )
            Text(
                text = version,
                modifier = Modifier.padding(padding.versionPadding),
                color = colors.versionColor,
                style = textStyles.versionStyle,
            )
            HorizontalPager(state = pagerState) {
                Text(
                    text = info[pagerState.currentPage],
                    modifier = Modifier
                        .padding(padding.infoPadding)
                        .height(extras.infoHeight)
                        .verticalScroll(rememberScrollState()),
                    color = colors.infoColor,
                    style = textStyles.infoStyle,
                )
            }
            if (info.size > 1) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = info.size,
                    modifier = Modifier.padding(padding.pageIndicatorPadding),
                    activeColor = colors.pagerIndicatorColors.activeColor,
                    inactiveColor = colors.pagerIndicatorColors.inactiveColor,
                    indicatorWidth = extras.pagerIndicatorExtras.indicatorWidth,
                    indicatorHeight = extras.pagerIndicatorExtras.indicatorHeight,
                    indicatorSpacing = extras.pagerIndicatorExtras.indicatorSpacing,
                    indicatorShape = extras.pagerIndicatorExtras.indicatorShape,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(padding.dividerPadding),
                thickness = extras.dividerThickness,
                color = colors.dividerColor,
            )
        }
    }
}

@Composable
internal fun SharedTransitionScope.LibraryList(
    animatedContentScope: AnimatedContentScope,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (Library) -> Unit,
    colors: AboutColors,
    padding: AboutPadding,
    extras: AboutExtras,
    textStyles: AboutTextStyles,
) {
    val scaleSpring = spring<Float>(stiffness = Spring.StiffnessMedium)

    LazyColumn(
        modifier = Modifier
            .padding(padding.libraryListPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(extras.itemSpacing),
    ) {
        libraries.forEach { (info, libs) ->
            item {
                with(animatedContentScope) {
                    Text(
                        text = sRes(info.headerId),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateEnterExit(
                                enter = scaleIn(scaleSpring),
                                exit = scaleOut(scaleSpring),
                            ),
                        color = colors.libraryHeaderColor,
                        style = textStyles.libraryHeaderStyle,
                    )
                }
            }
            items(
                items = libs,
                key = { it.uniqueId },
            ) { library ->
                LibraryDetails(
                    animatedContentScope = animatedContentScope,
                    isFullscreen = false,
                    actionOnClick = { libraryOnClick(library) },
                    library = library,
                    colors = colors.libraryItemColors,
                    padding = padding.libraryItemPadding,
                    extras = extras.libraryItemExtras,
                    textStyles = textStyles.libraryItemStyles,
                )
            }
        }
    }
}