package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.unit.dp
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
    icon: @Composable () -> Unit = {
        Icon(painter = pRes(R.drawable.ic_launcher_foreground), contentDescription = null)
    },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    separateByParty: Boolean = true,
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    AboutScreen(
        icon = icon,
        title = title,
        version = version,
        info = info,
        libraries = libraries,
        colors = colors,
        padding = padding,
        dimensions = dimensions,
        textStyles = textStyles,
    )
}

@Composable
fun AboutScreen(
    icon: @Composable () -> Unit = {
        Icon(painter = pRes(R.drawable.ic_launcher_foreground), contentDescription = null)
    },
    title: String = "App name",
    version: String = "1.0.0",
    info: List<String> = listOf(),
    libraries: Pair<List<Library>, List<Library>>,
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
            libraries = libraries,
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
                    .height(100.dp)
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
            thickness = dimensions.dividerWidth,
            color = colors.dividerColor,
        )
    }
}

@Composable
internal fun LibraryList(
    libraries: Pair<List<Library>, List<Library>>,
    colors: AboutColors = AboutDefaults.aboutColors(),
    padding: AboutPadding = AboutDefaults.aboutPadding(),
    dimensions: AboutDimensions = AboutDefaults.aboutDimensions(),
    textStyles: AboutTextStyles = AboutDefaults.aboutTextStyles(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimensions.libraryItemSpacing)
    ) {
        item {
            Text(
                text = if (libraries.second.isNotEmpty()) {
                    sRes(R.string.about_third_party_header)
                } else {
                    sRes(R.string.about_all_header)
                },
                modifier = Modifier.fillMaxWidth(),
                color = colors.libraryHeaderColor,
                style = textStyles.libraryHeaderStyle,
            )
        }
        items(libraries.first) {
            LibraryItem(
                library = it,
                colors = colors.libraryColors,
                padding = padding.libraryPadding,
                dimensions = dimensions.libraryDimensions,
                textStyles = textStyles.libraryStyles,
            )
        }
        if (libraries.second.isNotEmpty()) {
            item {
                Text(
                    text = sRes(R.string.about_first_party_header),
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.libraryHeaderColor,
                    style = textStyles.libraryHeaderStyle,
                )
            }
            items(libraries.second) {
                LibraryItem(
                    library = it,
                    colors = colors.libraryColors,
                    padding = padding.libraryPadding,
                    dimensions = dimensions.libraryDimensions,
                    textStyles = textStyles.libraryStyles,
                )
            }
        }
    }
}

@Composable
internal fun LibraryItem(
    library: Library,
    colors: LibraryColors = LibraryDefaults.libraryColors(),
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
                    .align(Alignment.CenterHorizontally),
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
                    !firstPartyIds.any { library.uniqueId.contains(it) }
                }
            } else {
                Pair(listOf(), libs.libraries)
            }
        }
        
    }
}