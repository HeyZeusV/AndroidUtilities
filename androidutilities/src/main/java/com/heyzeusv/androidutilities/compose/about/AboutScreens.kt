package com.heyzeusv.androidutilities.compose.about

import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
import kotlinx.serialization.Serializable

/**
 *  The screens available to [AboutScreen]'s NavGraph.
 */
internal sealed interface AboutScreens {
    /**
     *  Displays app info and libraries.
     */
    @Serializable
    data object Overview : AboutScreens

    /**
     *  Displays full information on [Library] belonging to [libraryGroup] with [libraryId].
     *
     *  @param libraryGroup Group/list that selected [Library] belongs to.
     *  @param libraryId Unique id that belongs to selected [Library].
     */
    @Serializable
    data class Library(val libraryGroup: LibraryGroup, val libraryId: String) : AboutScreens
}