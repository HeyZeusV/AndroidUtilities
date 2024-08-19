@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.runtime.Composable

private const val SURFACE_KEY = "library-surface-"
private const val NAME_KEY = "library-name-"
private const val DEVELOPER_KEY = "library-developer-"
private const val PAGER_KEY = "library-pager-"
private const val PAGER_INDICATOR_KEY = "library-pager-indicator-"

@Composable
fun SharedTransitionScope.librarySCS(
    sharedContent: LibrarySharedContent,
    libraryId: String,
): SharedContentState = rememberSharedContentState("${sharedContent.key}$libraryId")

enum class LibrarySharedContent(val key: String) {
    SURFACE(SURFACE_KEY),
    NAME(NAME_KEY),
    DEVELOPER(DEVELOPER_KEY),
    PAGER(PAGER_KEY),
    PAGER_INDICATOR(PAGER_INDICATOR_KEY),
}