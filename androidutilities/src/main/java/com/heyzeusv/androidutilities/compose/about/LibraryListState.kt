package com.heyzeusv.androidutilities.compose.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ANDROID = "androidx"
private const val JETBRAINS = "org.jetbrains"
private const val GOOGLE = "com.google"
private val firstPartyIds = listOf(ANDROID, JETBRAINS, GOOGLE)

@Composable
fun produceLibraryListState(
    separateByParty: Boolean,
): State<ImmutableMap<LibraryGroup, ImmutableList<Library>>> {
    val context = LocalContext.current

    return produceState(persistentMapOf(LibraryGroup.INIT to persistentListOf())) {
        value = withContext(Dispatchers.IO) {
            val libs = Libs.Builder().withContext(context).build()
            if (separateByParty) {
                val thirdLibs = (libs.libraries as PersistentList).mutate { libList ->
                    libList.removeIf { library ->
                        firstPartyIds.any { firstPartyId ->
                            library.uniqueId.contains(firstPartyId)
                        }
                    }
                }
                val firstLibs = (libs.libraries as PersistentList).mutate { libList ->
                    thirdLibs.forEach { library ->
                        libList.remove(library)
                    }
                }

                persistentMapOf(LibraryGroup.THIRD to thirdLibs, LibraryGroup.FIRST to firstLibs)
            } else {
                persistentMapOf(LibraryGroup.ALL to libs.libraries)
            }
        }
    }
}