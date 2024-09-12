package com.heyzeusv.androidutilities.room

import com.squareup.kotlinpoet.TypeName

fun List<String>.containsNullableType(element: TypeName): Boolean {
    val sElement = element.toString()
    return this.contains(sElement) || this.contains(sElement.removeSuffix("?"))
}

fun TypeName.equalsNullableType(type: TypeName): Boolean {
    return this == type ||
            this.toString() == "$type?" ||
            this.toString() == type.toString().removeSuffix("?")
}