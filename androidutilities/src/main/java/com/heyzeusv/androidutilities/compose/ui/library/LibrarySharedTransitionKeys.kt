@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.runtime.Composable

private const val NAME_KEY = "library-name-"
private const val DEVELOPER_KEY = "library-developer-"
private const val BODY_KEY = "library-body-"
private const val FOOTER_KEY = "library-footer-"

@Composable
fun SharedTransitionScope.libraryNameSharedContentState(libraryId: String): SharedContentState =
    rememberSharedContentState("$NAME_KEY$libraryId")

@Composable
fun SharedTransitionScope.libraryDeveloperSharedContentState(libraryId: String): SharedContentState =
    rememberSharedContentState("$DEVELOPER_KEY$libraryId")

@Composable
fun SharedTransitionScope.libraryBodySharedContentState(libraryId: String): SharedContentState =
    rememberSharedContentState("$BODY_KEY$libraryId")

@Composable
fun SharedTransitionScope.libraryFooterSharedContentState(libraryId: String): SharedContentState =
    rememberSharedContentState("$FOOTER_KEY$libraryId")