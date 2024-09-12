package com.heyzeusv.androidutilities.room

data class TypeConverterInfo(
    val packageName: String,
    val parentClass: String,
    val functionName: String,
    val parameterType: String,
    val returnType: String,
)