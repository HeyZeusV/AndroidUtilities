package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
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