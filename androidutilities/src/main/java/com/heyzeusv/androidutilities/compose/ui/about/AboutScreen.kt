@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.library.LibraryList
import com.heyzeusv.androidutilities.compose.ui.library.LibraryPartyInfo
import com.heyzeusv.androidutilities.compose.ui.pageindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.pRes
import com.mikepenz.aboutlibraries.entity.Library

@Composable
internal fun AboutScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    icon: @Composable () -> Unit = {
        Icon(painter = pRes(R.drawable.ic_launcher_foreground), contentDescription = null)
    },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (String, String) -> Unit = { _, _ -> },
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    Column(
        modifier = Modifier
            .padding(padding.contentPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        AppInfo(
            icon = icon,
            title = title,
            version = version,
            info = info,
            colors = colors,
            padding = padding,
            dimensions = dimensions,
            textStyles = textStyles,
        )
        LibraryList(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            libraries = libraries,
            libraryOnClick = libraryOnClick,
            bodyMaxLines = 5,
            colors = colors,
            padding = padding,
            dimensions = dimensions,
            textStyles = textStyles,
        )
    }
}

@Composable
internal fun AppInfo(
    icon: @Composable () -> Unit = {
        Icon(painter = pRes(R.drawable.ic_launcher_foreground), contentDescription = null)
    },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    val pagerState = rememberPagerState(pageCount = { info.size })

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.appInfoItemSpacing),
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
                    .height(dimensions.infoHeight)
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
                indicatorWidth = dimensions.pagerIndicatorDimensions.indicatorWidth,
                indicatorHeight = dimensions.pagerIndicatorDimensions.indicatorHeight,
                indicatorSpacing = dimensions.pagerIndicatorDimensions.indicatorSpacing,
                indicatorShape = dimensions.pagerIndicatorDimensions.indicatorShape,
            )
        }
        HorizontalDivider(
            thickness = dimensions.dividerThickness,
            color = colors.dividerColor,
        )
    }
}