package com.heyzeusv.androidutilities.compose.about.overview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

data class InfoEntry(
    val text: String,
    val textStyle: TextStyle? = null,
    val linkStyle: TextStyle? = null,
    val linkTextToHyperlinks: Map<String, String> = mapOf(),
    val linkTextColor: Color = Color.Blue,
    val linkTextFontWeight: FontWeight = FontWeight.Normal,
    val linkTextDecoration: TextDecoration = TextDecoration.Underline,
)