package com.heyzeusv.androidutilities.room

import com.squareup.kotlinpoet.ClassName

data class EntityData(
    val originalClassName: ClassName,
    val utilClassName: ClassName,
    val tableName: String,
    val fieldInfoList: List<FieldInfo>,
)
