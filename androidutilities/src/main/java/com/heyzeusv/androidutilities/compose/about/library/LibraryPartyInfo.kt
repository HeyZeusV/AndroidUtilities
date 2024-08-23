package com.heyzeusv.androidutilities.compose.about.library

import androidx.annotation.StringRes
import com.heyzeusv.androidutilities.R

// TODO: Create interface and extend it, so users can create custom enums if needed
enum class LibraryPartyInfo(
    val id: String,
    @StringRes val headerId: Int,
) {
    INIT(id = "", headerId = R.string.about_empty_header),
    FIRST(id = "first", headerId = R.string.about_first_party_header),
    THIRD(id = "third", headerId = R.string.about_third_party_header),
    ALL(id = "all", headerId = R.string.about_all_header),

    ;

    companion object {
        private val map = LibraryPartyInfo.entries.associateBy { it.id }
        infix fun from(id: String): LibraryPartyInfo = map[id] ?: INIT
    }
}