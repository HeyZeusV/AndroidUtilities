package com.heyzeusv.androidutilities.compose.about.library

import com.heyzeusv.androidutilities.compose.about.overview.AboutOverview

private const val SURFACE_PREFIX = "library-surface-"
private const val NAME_PREFIX = "library-name-"
private const val DEVELOPER_PREFIX = "library-developer-"
//private const val TOP_DIVIDER_PREFIX = "library-top-divider-"
//private const val BODY_PREFIX = "library-body-"
private const val FOOTER_PREFIX = "library-footer-"
//private const val PAGER_INDICATOR_PREFIX = "library-pager-indicator-"
private const val BOTTOM_DIVIDER_PREFIX = "library-bottom-divider-"

/**
 *  Represents the elements that animate when moving between [AboutOverview] and [AboutLibrary].
 *
 *  @param prefix Attached to library id and passed to sharedElement modifier to determine element
 *  start/end positions.
 */
internal enum class LibrarySharedContentKeyPrefix(val prefix: String) {
    SURFACE(SURFACE_PREFIX),
    NAME(NAME_PREFIX),
    DEVELOPER(DEVELOPER_PREFIX),
//    TOP_DIVIDER(TOP_DIVIDER_PREFIX),
//    BODY(BODY_PREFIX),
    FOOTER(FOOTER_PREFIX),
//    PAGER_INDICATOR(PAGER_INDICATOR_PREFIX),
    BOTTOM_DIVIDER(BOTTOM_DIVIDER_PREFIX),
}