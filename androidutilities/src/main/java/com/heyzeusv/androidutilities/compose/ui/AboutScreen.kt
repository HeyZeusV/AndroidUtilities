package com.heyzeusv.androidutilities.compose.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
fun AboutScreen(
    separateByParty: Boolean = true,
) {
    val libraries by produceLibraryState(separateByParty = separateByParty)
    Log.d("tag", "AboutScreen")
    Log.d("tag", "Libraries ${libraries.first.size}")
    Log.d("tag", "Libraries ${libraries.second.size}")
    Log.d("tag", "second ${libraries.second.map { it.name }}")
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