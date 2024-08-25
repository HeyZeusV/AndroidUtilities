package com.heyzeusv.androidutilitieslibrary

import kotlinx.serialization.Serializable

sealed interface Screens {
    @Serializable
    data object Overview: Screens

    @Serializable
    data object About : Screens

    @Serializable
    data object AboutNoIcon: Screens

    @Serializable
    data object AboutNoBackOrIcon: Screens

    @Serializable
    data object AnnotatedString: Screens

    @Serializable
    data object ComposableResources: Screens
}