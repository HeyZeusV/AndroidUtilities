package com.heyzeusv.androidutilities.room

internal enum class RoomTypes(val types: List<String>) {
    TO_ACCEPTED(listOf(
        "kotlin.Boolean", "kotlin.Short", "kotlin.Int", "kotlin.Long", "kotlin.Byte",
        "kotlin.String", "kotlin.Char", "kotlin.Double", "kotlin.Float", "kotlin.ByteArray"
    )),
    TO_COMPLEX(types = emptyList()),
}