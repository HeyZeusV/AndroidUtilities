package com.heyzeusv.androidutilities.room

import com.squareup.kotlinpoet.TypeName

data class TypeConverterInfo(
    val packageName: String,
    val parentClass: String,
    val functionName: String,
    val parameterType: TypeName,
    val returnType: TypeName,
)