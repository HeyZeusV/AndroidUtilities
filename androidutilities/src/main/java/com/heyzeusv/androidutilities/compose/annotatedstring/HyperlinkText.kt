package com.heyzeusv.androidutilities.compose.annotatedstring

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle,
    linkStyle: TextStyle? = null,
    linkTextToHyperlinks: ImmutableMap<String, String>,
    linkTextColor: Color = Color.Blue,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {
    val annotatedString = hyperlinkAnnotatedString(
        text = text,
        textStyle = textStyle,
        linkStyle = linkStyle,
        linkTextToHyperlinks = linkTextToHyperlinks,
        linkTextColor = linkTextColor,
        linkTextFontWeight = linkTextFontWeight,
        linkTextDecoration = linkTextDecoration,
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        lineHeight = textStyle.lineHeight.coerceAtLeast(linkStyle?.lineHeight),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = textStyle,
    )
}

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    @StringRes textId: Int,
    textStyle: TextStyle,
    linkStyle: TextStyle? = null,
    linkTextToHyperlinks: ImmutableMap<String, String>,
    linkTextColor: Color = Color.Blue,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {
    val annotatedString = hyperlinkAnnotatedString(
        context = LocalContext.current,
        textId = textId,
        textStyle = textStyle,
        linkStyle = linkStyle,
        linkTextToHyperlinks = linkTextToHyperlinks,
        linkTextColor = linkTextColor,
        linkTextFontWeight = linkTextFontWeight,
        linkTextDecoration = linkTextDecoration,
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        lineHeight = textStyle.lineHeight.coerceAtLeast(linkStyle?.lineHeight),
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        style = textStyle,
    )
}