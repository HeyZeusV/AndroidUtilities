package com.heyzeusv.androidutilities.compose.ui.about

import kotlinx.serialization.Serializable

sealed interface AboutScreens {
    @Serializable
    data object Overview : AboutScreens

    @Serializable
    data class LibraryDetails(val partyId: String, val libraryId: String) : AboutScreens
}