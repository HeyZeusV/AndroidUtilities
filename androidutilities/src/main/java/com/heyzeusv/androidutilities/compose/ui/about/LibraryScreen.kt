package com.heyzeusv.androidutilities.compose.ui.about

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.ui.HorizontalPagerIndicator
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
import com.heyzeusv.androidutilities.compose.util.sRes
import com.mikepenz.aboutlibraries.entity.Library

@Composable
fun LibraryScreen(
    modifier: Modifier,
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
        modifier = modifier,
        color = colors.backgroundColor,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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