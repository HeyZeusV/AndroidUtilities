package com.heyzeusv.androidutilities.compose.annotatedstring

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

fun hyperLinkAnnotatedString(
    text: String,
    textStyle: TextStyle,
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
    for ((linkText, hyperLink) in linkTextToHyperlinks) {
        val startIndex = text.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        addLink(
            url = LinkAnnotation.Url(
                url = hyperLink.checkForHttps(),
                styles = TextLinkStyles(
                    style = textStyle.copy(
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

private const val HTTPS = "https://"

private fun String.checkForHttps(): String {
    return if (!this.contains(HTTPS)) "$HTTPS$this" else this
}
