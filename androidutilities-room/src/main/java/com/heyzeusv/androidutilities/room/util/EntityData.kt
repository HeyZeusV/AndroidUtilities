package com.heyzeusv.androidutilities.room.util

import com.heyzeusv.androidutilities.room.creators.EntityFilesCreator
import com.squareup.kotlinpoet.ClassName

/**
 *  Contains information on Room.Entity annotated classes and is used to create RoomData.kt and
 *  CsvConverter.kt.
 *
 *  @param originalClassName [ClassName] of class annotated with Room.Entity.
 *  @param utilClassName [ClassName] of class created by [EntityFilesCreator].
 *  @param tableName Name of table when saved by Room
 *  @param fieldInfoList Information on each property belonging to entity
 */
data class EntityData(
    val originalClassName: ClassName,
    val utilClassName: ClassName,
    val tableName: String,
    val fieldInfoList: List<FieldInfo>,
)
