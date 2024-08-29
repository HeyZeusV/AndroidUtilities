package com.heyzeusv.androidutilities.compose.annotatedstring

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.collections.immutable.ImmutableMap

/**
 *  Displays [text] after it has been styled as an [AnnotatedString] in a [Text] Composable.
 *
 *  @param modifier Modifier applied to [Text] Composable.
 *  @param text String to be styled.
 *  @param textStyle Text style for non-hyperlink text.
 *  @param linkStyle Text style for hyperlink text.
 *  @param linkTextToHyperlinks Map of text to the link it connects to.
 *  @param linkTextColor Color of hyperlink text.
 *  @param linkTextFontWeight Weight of hyperlink text.
 *  @param linkTextDecoration Any decoration that should by applied to hyperlink text.
 *  @param overflow how visual overflow should be handled.
 *  @param softWrap whether the text should break at soft line breaks. If false, the glyphs in the
 *  text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 *  [overflow] and TextAlign may have unexpected effects.
 *  @param maxLines An optional maximum number of lines for the text to span, wrapping if
 *  necessary. If the text exceeds the given number of lines, it will be truncated according to
 *   [overflow] and [softWrap]. It is required that 1 <= [minLines] <= [maxLines].
 *  @param minLines The minimum height in terms of minimum number of visible lines. It is required
 *  that 1 <= [minLines] <= [maxLines].
 */
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

/**
 *  Displays [textId] after it has been styled as an [AnnotatedString] in a [Text] Composable.
 *
 *  @param modifier Modifier applied to [Text] Composable.
 *  @param textId Id of string resource to be styled.
 *  @param textStyle Text style for non-hyperlink text.
 *  @param linkStyle Text style for hyperlink text.
 *  @param linkTextToHyperlinks Map of text to the link it connects to.
 *  @param linkTextColor Color of hyperlink text.
 *  @param linkTextFontWeight Weight of hyperlink text.
 *  @param linkTextDecoration Any decoration that should by applied to hyperlink text.
 *  @param overflow how visual overflow should be handled.
 *  @param softWrap whether the text should break at soft line breaks. If false, the glyphs in the
 *  text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 *  [overflow] and TextAlign may have unexpected effects.
 *  @param maxLines An optional maximum number of lines for the text to span, wrapping if
 *  necessary. If the text exceeds the given number of lines, it will be truncated according to
 *   [overflow] and [softWrap]. It is required that 1 <= [minLines] <= [maxLines].
 *  @param minLines The minimum height in terms of minimum number of visible lines. It is required
 *  that 1 <= [minLines] <= [maxLines].
 */
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