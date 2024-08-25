package com.heyzeusv.androidutilities.compose.annotatedstring

import androidx.compose.ui.unit.TextUnit

fun TextUnit.coerceAtLeast(minimumValue: TextUnit?): TextUnit {
    if (minimumValue == null) return this
    return if (this.value < minimumValue.value) minimumValue else this
}