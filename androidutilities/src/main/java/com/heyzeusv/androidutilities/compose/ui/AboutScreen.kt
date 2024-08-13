package com.heyzeusv.androidutilities.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.heyzeusv.androidutilities.R
import com.heyzeusv.androidutilities.compose.util.ifNullOrBlank
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
    separateByParty: Boolean = true,
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(libraries.first) {
                LibraryItem(library = it)
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(libraries.second) {
                LibraryItem(library = it)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItem(
    library: Library,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val developers = library.developers.map { it.name }.joinToString(separator = ", ")
    val description = library.description.ifNullOrBlank(sRes(R.string.library_description_empty))
    val version = library.artifactVersion.ifNullOrBlank(sRes(R.string.library_version_empty))
    val license = library.licenses.firstOrNull()
    val licenseContent = license?.licenseContent.ifNullOrBlank(sRes(R.string.library_license_empty))
    val licenseName = license?.name.ifNullOrBlank(sRes(R.string.library_license_empty))


    Surface(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(text = library.name)
            Text(text = developers)
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> LibraryInfo(body = description, footer = version)
                    else -> LibraryInfo(body = licenseContent, footer = licenseName)
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = 2,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun LibraryInfo(
    body: String,
    footer: String,
) {
    Column {
        HorizontalDivider()
        Text(
            text = body,
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalDivider()
        Text(
            text = footer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
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
                    firstPartyIds.any { library.uniqueId.contains(it) }
                }
            } else {
                Pair(libs.libraries, listOf())
            }
        }
        
    }
}