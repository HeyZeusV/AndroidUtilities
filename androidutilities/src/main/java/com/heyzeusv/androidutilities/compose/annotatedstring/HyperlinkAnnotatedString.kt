package com.heyzeusv.androidutilities.compose.annotatedstring

import android.content.Context
import android.text.Annotation
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.toSpanned

private const val HTTPS = "https://"

private fun String.checkForHttps(): String {
    return if (!this.contains(HTTPS)) "$HTTPS$this" else this
}

fun hyperlinkAnnotatedString(
    text: String,
    textStyle: TextStyle,
    linkStyle: TextStyle? = null,
    linkTextToHyperlinks: Map<String, String>,
    linkTextColor: Color = Color.Blue,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
): AnnotatedString = buildAnnotatedString {
    append(text)
    addStyle(
        style = textStyle.toSpanStyle(),
        start = 0,
        end = text.length
    )
    for ((linkText, hyperlink) in linkTextToHyperlinks) {
        val startIndex = text.indexOf(linkText)
        val endIndex = startIndex + linkText.length
        val linkTextStyle = linkStyle ?: textStyle

        addLink(
            url = LinkAnnotation.Url(
                url = hyperlink.checkForHttps(),
                styles = TextLinkStyles(
                    style = linkTextStyle.copy(
                        color = linkTextColor,
                        fontWeight = linkTextFontWeight,
                        textDecoration = linkTextDecoration,
                    ).toSpanStyle()
                )
            ),
            start = startIndex,
            end = endIndex,
        )
    }
}

fun hyperlinkAnnotatedString(
    context: Context,
    @StringRes textId: Int,
    textStyle: TextStyle,
    linkStyle: TextStyle? = null,
    linkTextToHyperlinks: Map<String, String>,
    linkTextColor: Color = Color.Blue,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
): AnnotatedString = buildAnnotatedString {
    val text = context.getText(textId).toSpanned()
    val annotations = text.getSpans(0, text.length, Annotation::class.java)

    append(text)
    addStyle(
        style = textStyle.toSpanStyle(),
        start = 0,
        end = text.length
    )
    for ((linkText, hyperlink) in linkTextToHyperlinks) {
        annotations?.find { it.value == linkText }?.let {
            val startIndex = text.getSpanStart(it)
            val endIndex = text.getSpanEnd(it)
            val linkTextStyle = linkStyle ?: textStyle

            addLink(
                url = LinkAnnotation.Url(
                    url = hyperlink.checkForHttps(),
                    styles = TextLinkStyles(
                        style = linkTextStyle.copy(
                            color = linkTextColor,
                            fontWeight = linkTextFontWeight,
                            textDecoration = linkTextDecoration,
                        ).toSpanStyle()
                    )
                ),
                start = startIndex,
                end = endIndex,
            )
        }
    }
}