@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.about

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
import com.heyzeusv.androidutilities.compose.util.pRes
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
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    icon: @Composable () -> Unit = {
        Icon(painter = pRes(R.drawable.ic_launcher_foreground), contentDescription = null)
    },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    separateByParty: Boolean = true,
    libraryOnClick: (String, String) -> Unit = { _, _ -> },
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    AboutScreen(
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        icon = icon,
        title = title,
        version = version,
        info = info,
        libraries = libraries,
        libraryOnClick = libraryOnClick,
        colors = colors,
        padding = padding,
        dimensions = dimensions,
        textStyles = textStyles,
    )
}

@Composable
fun AboutScreen(
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

@Composable
internal fun LibraryList(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    libraries: Map<LibraryPartyInfo, List<Library>>,
    libraryOnClick: (String, String) -> Unit,
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
                    LibraryItem(
                        modifier = Modifier
                            .clickable { libraryOnClick(info.id, library.uniqueId) }
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(sharedKey),
                                animatedVisibilityScope = animatedContentScope
                            ),
                        library = library,
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
internal fun LibraryItem(
    modifier: Modifier,
    library: Library,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
    padding: LibraryPadding = LibraryDefaults.libraryPadding(),
    dimensions: LibraryDimensions = LibraryDefaults.libraryDimensions(),
    textStyles: LibraryTextStyles = LibraryDefaults.libraryTextStyles(),
) {
    val developers = library.developers.map { it.name }.joinToString(separator = ", ")
    val description = library.description.ifNullOrBlank(sRes(R.string.library_description_empty))
    val version = library.artifactVersion.ifNullOrBlank(sRes(R.string.library_version_empty))

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
            LibraryInfo(
                body = description,
                footer = version,
                colors = colors,
                padding = padding,
                dimensions = dimensions,
                textStyles = textStyles,
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
            thickness = dimensions.dividerThickness,
            color = colors.dividerColor,
        )
        Text(
            text = body,
            modifier = Modifier
                .padding(padding.bodyPadding)
                .fillMaxWidth(),
            color = colors.contentColor,
            maxLines = 5,
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

@Composable
fun produceLibraryState(separateByParty: Boolean): State<Map<LibraryPartyInfo, List<Library>>> {
    val context = LocalContext.current

    return produceState(mapOf(LibraryPartyInfo.INIT to listOf())) {
        value = withContext(Dispatchers.IO) {
            val libs = Libs.Builder().withContext(context).build()
            if (separateByParty) {
                val (thirdLibs, firstLibs) = libs.libraries.partition { library ->
                    !firstPartyIds.any { library.uniqueId.contains(it) }
                }
                mapOf(LibraryPartyInfo.THIRD to thirdLibs, LibraryPartyInfo.FIRST to firstLibs)
            } else {
                mapOf(LibraryPartyInfo.ALL to libs.libraries)
            }
        }

    }
}

enum class LibraryPartyInfo(
    val id: String,
    @StringRes val headerId: Int,
) {
    INIT(id = "", headerId = R.string.about_empty_header),
    FIRST(id = "first", headerId = R.string.about_first_party_header),
    THIRD(id = "third", headerId = R.string.about_third_party_header),
    ALL(id = "all", headerId = R.string.about_all_header),

    ;

    companion object {
        private val map = LibraryPartyInfo.entries.associateBy { it.id }
        infix fun from(id: String): LibraryPartyInfo = map[id] ?: INIT
    }
}