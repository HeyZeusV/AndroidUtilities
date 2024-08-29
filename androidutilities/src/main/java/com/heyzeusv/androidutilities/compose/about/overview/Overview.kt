@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.about.overview

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.heyzeusv.androidutilities.compose.about.library.LibraryDetails
import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
import com.heyzeusv.androidutilities.compose.annotatedstring.HyperlinkText
import com.heyzeusv.androidutilities.compose.pagerindicator.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf

/**
 *  Full screen Composable that splits the screen into two parts.
 *
 *  The top part displays information about the app: an [icon] (optional), [title], [version], and
 *  any other [InfoEntry] in a [infoList] (changelog, contact info, etc.).
 *
 *  The bottom part displays all libraries used with the help of [AboutLibraries](https://github.com/mikepenz/AboutLibraries),
 *  which does require including its plugin.
 *
 *  @param backButton Allows for back button if not using TopAppBar navigation.
 *  @param icon Optional icon displayed above [title] Text.
 *  @param title Title for screen.
 *  @param version Text meant to display current version of app.
 *  @param infoList String or String Resource, along with its styling, to be displayed in
 *  HorizontalPager.
 *  @param libraries Map of libraries where each [LibraryGroup] is paired to the list of
 *  [Library] to be displayed under it.
 *  @param libraryOnClick Expands [LibraryDetails] to full screen.
 *  @param colors Colors to be used.
 *  @param padding Padding to be used.
 *  @param extras Additional values to be used.
 *  @param textStyles Text Styles to be used.
 */
context(AnimatedContentScope)
@Composable
internal fun SharedTransitionScope.AboutOverview(
    animatedContentScope: AnimatedContentScope,
    backButton: @Composable () -> Unit = { },
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: String = "App name",
    version: String = "1.0.0",
    infoList: ImmutableList<InfoEntry> = persistentListOf(),
    libraries: ImmutableMap<LibraryGroup, ImmutableList<Library>>,
    libraryOnClick: (LibraryGroup, String) -> Unit,
    colors: OverviewColors = OverviewDefaults.overviewColors(),
    padding: OverviewPadding = OverviewDefaults.overviewPadding(),
    extras: OverviewExtras = OverviewDefaults.overviewExtras(),
    textStyles: OverviewTextStyles = OverviewDefaults.overviewTextStyles(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        AppInfo(
            backButton = backButton,
            icon = icon,
            title = title,
            version = version,
            infoList = infoList,
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

/**
 *  Top portion of [AboutOverview] that displays information about the app: an [icon] (optional),
 *  [title], [version], and any other [InfoEntry] in a [infoList] (changelog, contact info, etc.).
 *
 *  @param backButton Allows for back button if not using TopAppBar navigation.
 *  @param icon Optional icon displayed above [title] Text.
 *  @param title Title for screen.
 *  @param version Text meant to display current version of app.
 *  @param infoList String or String Resource, along with its styling, to be displayed in
 *  HorizontalPager.
 *  @param colors Colors to be used.
 *  @param padding Padding to be used.
 *  @param extras Additional values to be used.
 *  @param textStyles Text Styles to be used.
 */
context(AnimatedContentScope)
@Composable
internal fun SharedTransitionScope.AppInfo(
    backButton: @Composable () -> Unit = { },
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: String = "App name",
    version: String = "1.0.0",
    infoList: ImmutableList<InfoEntry> = persistentListOf(),
    colors: OverviewColors = OverviewDefaults.overviewColors(),
    padding: OverviewPadding = OverviewDefaults.overviewPadding(),
    extras: OverviewExtras = OverviewDefaults.overviewExtras(),
    textStyles: OverviewTextStyles = OverviewDefaults.overviewTextStyles(),
) {
    val pagerState = rememberPagerState(pageCount = { infoList.size })

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
            Box(modifier = Modifier.fillMaxWidth()) {
                icon?.let {
                    backButton()
                    it()
                }
            }
            Row {
                if (icon == null) backButton()
                Text(
                    text = title,
                    modifier = Modifier.padding(padding.titlePadding),
                    color = colors.titleColor,
                    style = textStyles.titleStyle,
                )
            }
            Text(
                text = version,
                modifier = Modifier.padding(padding.versionPadding),
                color = colors.versionColor,
                style = textStyles.versionStyle,
            )
            HorizontalPager(state = pagerState) { page ->
                when (val info = infoList[page]) {
                    is StringInfoEntry -> {
                        HyperlinkText(
                            modifier = Modifier
                                .padding(padding.infoPadding)
                                .height(extras.infoHeight)
                                .verticalScroll(rememberScrollState()),
                            text = info.text,
                            textStyle = info.textStyle ?: textStyles.infoStyle,
                            linkStyle = info.linkStyle,
                            linkTextToHyperlinks = info.linkTextToHyperlinks,
                            linkTextColor = info.linkTextColor,
                            linkTextFontWeight = info.linkTextFontWeight,
                            linkTextDecoration = info.linkTextDecoration,
                        )
                    }
                    is StringResourceInfoEntry -> {
                        HyperlinkText(
                            modifier = Modifier
                                .padding(padding.infoPadding)
                                .height(extras.infoHeight)
                                .verticalScroll(rememberScrollState()),
                            textId = info.textId,
                            textStyle = info.textStyle ?: textStyles.infoStyle,
                            linkStyle = info.linkStyle,
                            linkTextToHyperlinks = info.linkTextToHyperlinks,
                            linkTextColor = info.linkTextColor,
                            linkTextFontWeight = info.linkTextFontWeight,
                            linkTextDecoration = info.linkTextDecoration,
                        )
                    }
                }
            }
            if (infoList.size > 1) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = infoList.size,
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

/**
 *  Bottom portion of [AboutOverview] that displays libraries used by app separated by
 *  [LibraryGroup].
 *
 *  @param animatedContentScope Scope used to animate shared elements between screens.
 *  @param libraries Map of libraries where each [LibraryGroup] is paired to the list of
 *  [Library] to be displayed under it.
 *  @param libraryOnClick Expands [LibraryDetails] to full screen.
 *  @param colors Colors to be used.
 *  @param padding Padding to be used.
 *  @param extras Additional values to be used.
 *  @param textStyles Text Styles to be used.
 */
@Composable
internal fun SharedTransitionScope.LibraryList(
    animatedContentScope: AnimatedContentScope,
    libraries: ImmutableMap<LibraryGroup, ImmutableList<Library>>,
    libraryOnClick: (LibraryGroup, String) -> Unit,
    colors: OverviewColors,
    padding: OverviewPadding,
    extras: OverviewExtras,
    textStyles: OverviewTextStyles,
) {
    val scaleSpring = spring<Float>(stiffness = Spring.StiffnessMedium)

    LazyColumn(
        modifier = Modifier
            .padding(padding.libraryListPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(extras.itemSpacing),
    ) {
        libraries.forEach { (libraryGroup, libs) ->
            item {
                with(animatedContentScope) {
                    Text(
                        text = sRes(libraryGroup.headerId),
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
                    actionOnClick = { libraryOnClick(libraryGroup, library.uniqueId) },
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