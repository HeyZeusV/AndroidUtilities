package com.heyzeusv.androidutilities.room

internal enum class RoomTypes(val types: List<String>) {
    ACCEPTED(listOf(
        "kotlin.Boolean", "kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Byte",
        "kotlin.String", "kotlin.Char", "kotlin.Double", "kotlin.Float", "kotlin.ByteArray"
    )),
    COMPLEX(types = emptyList()),
}