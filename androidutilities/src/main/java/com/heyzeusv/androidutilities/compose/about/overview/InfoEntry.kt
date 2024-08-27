package com.heyzeusv.androidutilities.compose.about.overview

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

@Stable
interface InfoEntry {
    val textStyle: TextStyle?
    val linkStyle: TextStyle?
    val linkTextToHyperlinks: ImmutableMap<String, String>
    val linkTextColor: Color
    val linkTextFontWeight: FontWeight
    val linkTextDecoration: TextDecoration
}

@Immutable
data class StringInfoEntry(
    val text: String,
    override val textStyle: TextStyle? = null,
    override val linkStyle: TextStyle? = null,
    override val linkTextToHyperlinks: ImmutableMap<String, String> = persistentMapOf( ),
    override val linkTextColor: Color = Color.Blue,
    override val linkTextFontWeight: FontWeight = FontWeight.Normal,
    override val linkTextDecoration: TextDecoration = TextDecoration.Underline,
) : InfoEntry

@Immutable
data class StringResourceInfoEntry(
    @StringRes val textId: Int,
    override val textStyle: TextStyle? = null,
    override val linkStyle: TextStyle? = null,
    override val linkTextToHyperlinks: ImmutableMap<String, String> = persistentMapOf(),
    override val linkTextColor: Color = Color.Blue,
    override val linkTextFontWeight: FontWeight = FontWeight.Normal,
    override val linkTextDecoration: TextDecoration = TextDecoration.Underline,
) : InfoEntry