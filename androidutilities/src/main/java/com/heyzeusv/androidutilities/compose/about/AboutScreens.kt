package com.heyzeusv.androidutilities.compose.about

import com.heyzeusv.androidutilities.compose.about.library.LibraryGroup
import kotlinx.serialization.Serializable

internal sealed interface AboutScreens {
    @Serializable
    data object Overview : AboutScreens

    @Serializable
    data class Library(val libraryGroup: LibraryGroup, val libraryId: String) : AboutScreens
}