package com.heyzeusv.androidutilities.compose.about.library

import com.heyzeusv.androidutilities.R

/**
 *  Represents the possible Library groups.
 *
 *  @param headerId Id of string resource to be displayed on header Text separating each group.
 */
enum class LibraryGroup(val headerId: Int)  {
    // group used while libraries are being loaded
    INIT(headerId = R.string.about_empty_header),
    // libraries belong to Google, Kotlin, JetBrains
    FIRST(headerId = R.string.about_first_party_header),
    // every other library
    THIRD(headerId = R.string.about_third_party_header),
    // all libraries together
    ALL(headerId = R.string.about_all_header),
}