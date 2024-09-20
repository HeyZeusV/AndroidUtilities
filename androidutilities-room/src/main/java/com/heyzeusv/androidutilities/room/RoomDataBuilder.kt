package com.heyzeusv.androidutilities.room

import com.heyzeusv.androidutilities.room.csv.csvMapClassName
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.getListTypeName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.buildRoomData(
    entityDataList: List<EntityData>,
): TypeSpec.Builder {
    addModifiers(KModifier.DATA)

    val constructorBuilder = FunSpec.constructorBuilder()
    val csvDataMapCodeBlock = CodeBlock.builder().add("mapOf(")

    entityDataList.forEachIndexed { index, entity  ->
        val keyDataName = entity.originalClassName.getDataName()
        val keyParameterSpec = ParameterSpec.builder(keyDataName, entity.originalClassName.getListTypeName())
            .defaultValue("emptyList()")
        constructorBuilder.addParameter(keyParameterSpec.build())
        val keyPropertySpec = PropertySpec.builder(keyDataName, entity.originalClassName.getListTypeName())
            .initializer(keyDataName)
        addProperty(keyPropertySpec.build())

        val valuePropertySpec = PropertySpec
            .builder(entity.utilClassName.getDataName(), entity.utilClassName.getListTypeName())
            .initializer("%L.map { %L.toUtil(it) }", keyDataName, entity.utilClassName.simpleName)
        addProperty(valuePropertySpec.build())

        csvDataMapCodeBlock.add("\n${entity.utilClassName.simpleName} to ${entity.utilClassName.getDataName()}")
        if (index != entityDataList.size) csvDataMapCodeBlock.add(",")
    }

    csvDataMapCodeBlock.add("\n)")
    val csvDataMapPropertySpec = PropertySpec.builder("csvDataMap", csvMapClassName)
        .initializer(csvDataMapCodeBlock.build())

    primaryConstructor(constructorBuilder.build())
        .addProperty(csvDataMapPropertySpec.build())

    return this
}