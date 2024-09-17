package com.heyzeusv.androidutilities.room.csv

import com.heyzeusv.androidutilities.room.EntityData
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

internal const val CONTEXT_PROP = "context"
internal val contextClassName = ClassName("android.content", "Context")
private val stringListClass = ClassName("kotlin.collections", "List")
    .parameterizedBy(String::class.asTypeName())

internal fun TypeSpec.Builder.buildCsvConverter(
    roomDataClassName: ClassName,
    entityDataList: List<EntityData>,
): TypeSpec.Builder {
    primaryConstructor(
        FunSpec.constructorBuilder()
            .addParameter(CONTEXT_PROP, contextClassName)
            .build()
    )
    addProperty(
        PropertySpec.builder(CONTEXT_PROP, contextClassName)
            .initializer(CONTEXT_PROP)
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    addProperty(
        PropertySpec.builder("csvFileNames", stringListClass)
            .initializer(buildCodeBlock {
                add("listOf(\n")
                val tableNames = entityDataList.map { it.tableName }
                tableNames.forEachIndexed { index, tableName ->
                    add("%S", "$tableName.csv")
                    if (index < tableNames.size) add(", ")
                }
                add("\n)")
            })
            .addModifiers(KModifier.PRIVATE)
            .build()
    )

    addFunction(importCsvToRoomFunSpec(roomDataClassName, entityDataList).build())
    addFunction(importCsvToRoomEntityFunSpec(entityDataList).build())
    addFunction(exportRoomToCsvFunSpec(roomDataClassName).build())
    addFunction(exportRoomEntityToCsvFunSpec().build())
    addFunction(createNewExportDirectoryFunSpec().build())
    addFunction(findOrCreateParentDirectoryFunSpec().build())
    return this
}