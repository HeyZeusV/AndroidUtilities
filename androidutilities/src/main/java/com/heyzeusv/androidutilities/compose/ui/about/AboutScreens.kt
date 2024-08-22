package com.heyzeusv.androidutilities.compose.ui.about

import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.serialization.Serializable

sealed interface AboutScreens {
    @Serializable
    data object Overview : AboutScreens

    @Serializable
    data class LibraryDetails(val library: Library) : AboutScreens
}