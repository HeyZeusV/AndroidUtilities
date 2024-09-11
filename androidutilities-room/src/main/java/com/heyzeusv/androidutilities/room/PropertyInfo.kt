package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.symbol.KSClassDeclaration

interface PropertyInfo {
    val name: String
}

data class FieldInfo(
    override val name: String,
    val fieldName: String,
) : PropertyInfo

data class EmbeddedInfo(
    override val name: String,
    val embeddedClass: KSClassDeclaration,
) : PropertyInfo

