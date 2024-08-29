package com.heyzeusv.androidutilities.compose.annotatedstring

import androidx.compose.ui.unit.TextUnit

/**
 *  Ensures that this' value is not less than the specified [minimumValue].
 *
 *  @param minimumValue [TextUnit] to compare to this.
 *  @return this if it's greater than or equal to the [minimumValue]'s value or [minimumValue]
 *  otherwise.
 */
fun TextUnit.coerceAtLeast(minimumValue: TextUnit?): TextUnit {
    if (minimumValue == null) return this
    return if (this.value < minimumValue.value) minimumValue else this
}

private const val HTTPS = "https://"

/**
 *  Urls are required to have [HTTPS] prefix in order for intent to open link to work.
 *
 *  @return this if it starts with [HTTPS] else appends [HTTPS] as prefix to this.
 */
internal fun String.checkForHttps(): String {
    return if (!this.startsWith(HTTPS)) "$HTTPS$this" else this
}