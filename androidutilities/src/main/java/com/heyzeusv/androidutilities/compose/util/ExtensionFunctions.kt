package com.heyzeusv.androidutilities.compose.util

import androidx.core.text.HtmlCompat

fun String?.ifNullOrBlank(defaultValue: String): String {
    return if (this.isNullOrBlank()) defaultValue else this
}

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