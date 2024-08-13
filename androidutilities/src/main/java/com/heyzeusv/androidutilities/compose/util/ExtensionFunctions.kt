package com.heyzeusv.androidutilities.compose.util

fun String?.ifNullOrBlank(defaultValue: String): String {
    return if (this.isNullOrBlank()) defaultValue else this
}