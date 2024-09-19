package com.heyzeusv.androidutilities.room

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal enum class RoomTypes(val types: List<TypeName>) {
    TO_ACCEPTED(listOf(
        ClassName("kotlin", "Boolean"), ClassName("kotlin", "Short"),
        ClassName("kotlin", "Int"), ClassName("kotlin", "Long"),
        ClassName("kotlin", "Byte"), ClassName("kotlin", "String"),
        ClassName("kotlin", "Char"), ClassName("kotlin", "Double"),
        ClassName("kotlin", "Float"), ClassName("kotlin", "ByteArray"),
    )),
    TO_COMPLEX(types = emptyList()),
}