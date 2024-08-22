@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.ui.library

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.runtime.Composable

private const val SURFACE_KEY = "library-surface-"
private const val NAME_KEY = "library-name-"
private const val DEVELOPER_KEY = "library-developer-"
//private const val TOP_DIVIDER_KEY = "library-top-divider-"
//private const val BODY_KEY = "library-body-"
//private const val MIDDLE_DIVIDER_KEY = "library-middle-divider-"
private const val FOOTER_KEY = "library-footer-"
//private const val PAGER_INDICATOR_KEY = "library-pager-indicator-"
private const val BOTTOM_DIVIDER_KEY = "library-bottom-divider-"

@Composable
fun SharedTransitionScope.librarySCS(
    prefix: LibrarySharedContentPrefix,
    key: String,
): SharedContentState = rememberSharedContentState("${prefix.prefix}$key")

enum class LibrarySharedContentPrefix(val prefix: String) {
    SURFACE(SURFACE_KEY),
    NAME(NAME_KEY),
    DEVELOPER(DEVELOPER_KEY),
//    TOP_DIVIDER(TOP_DIVIDER_KEY),
//    BODY(BODY_KEY),
//    MIDDLE_DIVIDER(MIDDLE_DIVIDER_KEY),
    FOOTER(FOOTER_KEY),
//    PAGER_INDICATOR(PAGER_INDICATOR_KEY),
    BOTTOM_DIVIDER(BOTTOM_DIVIDER_KEY),
}