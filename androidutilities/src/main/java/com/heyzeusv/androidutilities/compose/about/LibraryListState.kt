package com.heyzeusv.androidutilities.compose.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
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
fun produceLibraryListState(separateByParty: Boolean): State<Map<LibraryGroup, List<Library>>> {
    val context = LocalContext.current

    return produceState(mapOf(LibraryGroup.INIT to listOf())) {
        value = withContext(Dispatchers.IO) {
            val libs = Libs.Builder().withContext(context).build()
            if (separateByParty) {
                val (thirdLibs, firstLibs) = libs.libraries.partition { library ->
                    !firstPartyIds.any { library.uniqueId.contains(it) }
                }
                mapOf(LibraryGroup.THIRD to thirdLibs, LibraryGroup.FIRST to firstLibs)
            } else {
                mapOf(LibraryGroup.ALL to libs.libraries)
            }
        }
    }
}