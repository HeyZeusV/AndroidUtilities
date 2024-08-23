@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.about.library

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.runtime.Composable

@Composable
internal fun SharedTransitionScope.librarySCS(
    prefix: LibrarySharedContentKeyPrefix,
    key: String,
): SharedContentState = rememberSharedContentState("${prefix.prefix}$key")