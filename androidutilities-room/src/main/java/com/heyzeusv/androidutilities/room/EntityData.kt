package com.heyzeusv.androidutilities.room

import com.squareup.kotlinpoet.ClassName

data class EntityData(
    val utilClassName: ClassName,
    val tableName: String,
    val fieldToTypeMap: Map<String, String>,
)
