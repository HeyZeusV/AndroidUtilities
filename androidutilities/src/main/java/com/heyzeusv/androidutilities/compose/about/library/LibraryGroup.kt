package com.heyzeusv.androidutilities.compose.about.library

import com.heyzeusv.androidutilities.R

enum class LibraryGroup(val headerId: Int)  {
    INIT(headerId = R.string.about_empty_header),
    FIRST(headerId = R.string.about_first_party_header),
    THIRD(headerId = R.string.about_third_party_header),
    ALL(headerId = R.string.about_all_header),
}