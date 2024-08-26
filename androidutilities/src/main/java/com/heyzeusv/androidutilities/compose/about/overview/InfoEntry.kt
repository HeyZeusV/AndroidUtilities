package com.heyzeusv.androidutilities.compose.about.overview

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

interface InfoEntry {
    val textStyle: TextStyle?
    val linkStyle: TextStyle?
    val linkTextToHyperlinks: Map<String, String>
    val linkTextColor: Color
    val linkTextFontWeight: FontWeight
    val linkTextDecoration: TextDecoration
}

data class StringInfoEntry(
    val text: String,
    override val textStyle: TextStyle? = null,
    override val linkStyle: TextStyle? = null,
    override val linkTextToHyperlinks: Map<String, String> = mapOf(),
    override val linkTextColor: Color = Color.Blue,
    override val linkTextFontWeight: FontWeight = FontWeight.Normal,
    override val linkTextDecoration: TextDecoration = TextDecoration.Underline,
) : InfoEntry

data class StringResourceInfoEntry(
    @StringRes val textId: Int,
    override val textStyle: TextStyle? = null,
    override val linkStyle: TextStyle? = null,
    override val linkTextToHyperlinks: Map<String, String> = mapOf(),
    override val linkTextColor: Color = Color.Blue,
    override val linkTextFontWeight: FontWeight = FontWeight.Normal,
    override val linkTextDecoration: TextDecoration = TextDecoration.Underline,
) : InfoEntry