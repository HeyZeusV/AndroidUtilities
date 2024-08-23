package com.heyzeusv.androidutilities.compose.about

import kotlinx.serialization.Serializable

sealed interface AboutScreens {
    @Serializable
    data object Overview : AboutScreens

    @Serializable
    data class Library(val partyId: String, val libraryId: String) : AboutScreens
}