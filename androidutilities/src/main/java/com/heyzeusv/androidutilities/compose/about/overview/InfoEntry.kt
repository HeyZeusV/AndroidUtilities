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

/**
 *  Represents hyperlinks and styling values needed for info list in [AppInfo].
 *
 *  @property textStyle Text style of non-hyperlink text.
 *  @property linkStyle Text style of hyperlink text.
 *  @property linkTextToHyperlinks Map of text to the link its connects to.
 *  @property linkTextColor Color of hyperlink text.
 *  @property linkTextFontWeight Weight of hyperlink text.
 *  @property linkTextDecoration Any decoration that should applied to hyperlink text.
 */
@Stable
interface InfoEntry {
    val textStyle: TextStyle?
    val linkStyle: TextStyle?
    val linkTextToHyperlinks: ImmutableMap<String, String>
    val linkTextColor: Color
    val linkTextFontWeight: FontWeight
    val linkTextDecoration: TextDecoration
}

/**
 *  Used when text to be styled is a [String].
 *
 *  @param text Text to be styled.
 *  @param textStyle Text style of non-hyperlink text.
 *  @param linkStyle Text style of hyperlink text.
 *  @param linkTextToHyperlinks Map of text to the link its connects to.
 *  @param linkTextColor Color of hyperlink text.
 *  @param linkTextFontWeight Weight of hyperlink text.
 *  @param linkTextDecoration Any decoration that should applied to hyperlink text.
 */
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

/**
 *  Used when text to be styled is a string resource.
 *
 *  @param textId Id of string resource to be styled.
 *  @param textStyle Text style of non-hyperlink text.
 *  @param linkStyle Text style of hyperlink text.
 *  @param linkTextToHyperlinks Map of text to the link its connects to.
 *  @param linkTextColor Color of hyperlink text.
 *  @param linkTextFontWeight Weight of hyperlink text.
 *  @param linkTextDecoration Any decoration that should applied to hyperlink text.
 */
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