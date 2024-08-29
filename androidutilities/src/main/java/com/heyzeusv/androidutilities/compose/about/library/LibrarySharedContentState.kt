@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.heyzeusv.androidutilities.compose.about.library

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.runtime.Composable

/**
 *  Creates [SharedContentState], which is passed to sharedElement modifier and used to determine
 *  element start/end position.
 *
 *  @param prefix The Composable string value.
 *  @param key Library id.
 */
@Composable
internal fun SharedTransitionScope.librarySCS(
    prefix: LibrarySharedContentKeyPrefix,
    key: String,
): SharedContentState = rememberSharedContentState("${prefix.prefix}$key")