package com.heyzeusv.androidutilities.compose.about

import androidx.core.text.HtmlCompat

/**
 *  A version of [String.ifBlank] that works on nullable [String]. Returns this [String] if it is
 *  not null, not empty, and doesn't consist solely of whitespace characters, or [defaultValue]
 *  otherwise.
 */
fun String?.ifNullOrBlank(defaultValue: String): String {
    return if (this.isNullOrBlank()) defaultValue else this
}

/**
 *  Formats String by removing new line characters that are in the middle of sentences using Regex.
 */
fun String.formatContent(): String {
    val removeSpacing = HtmlCompat.fromHtml(
        this.replace("\n", "<br/>"),
        HtmlCompat.FROM_HTML_MODE_COMPACT,
    ).toString()
    val removeCertainLineBreaks = removeSpacing.replace(
        regex = Regex("(\\S)[ \\t]*(?:\\r\\n|\\n)[ \\t]*(\\S)"),
        transform = { match -> match.value.replace(Regex("\\s"), " ") }
    )
    return removeCertainLineBreaks
}