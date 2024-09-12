package com.heyzeusv.androidutilities.room

fun List<String>.containsNullableType(element: String): Boolean {
    return this.contains(element) || this.contains(element.removeSuffix("?"))
}