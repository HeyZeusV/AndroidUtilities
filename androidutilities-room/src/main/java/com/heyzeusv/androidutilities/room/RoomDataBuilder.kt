package com.heyzeusv.androidutilities.room

import com.heyzeusv.androidutilities.room.csv.csvMapClassName
import com.heyzeusv.androidutilities.room.util.getDataName
import com.heyzeusv.androidutilities.room.util.getListTypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal fun TypeSpec.Builder.buildRoomData(
    classNameMap: MutableMap<ClassName, ClassName>,
): TypeSpec.Builder {
    addModifiers(KModifier.DATA)

    val constructorBuilder = FunSpec.constructorBuilder()
    val csvDataMapCodeBlock = CodeBlock.builder().add("mapOf(")

    classNameMap.entries.forEachIndexed { index, entry ->
        val keyDataName = entry.key.getDataName()
        val keyParameterSpec = ParameterSpec.builder(keyDataName, entry.key.getListTypeName())
            .defaultValue("emptyList()")
        constructorBuilder.addParameter(keyParameterSpec.build())
        val keyPropertySpec = PropertySpec.builder(keyDataName, entry.key.getListTypeName())
            .initializer(keyDataName)
        addProperty(keyPropertySpec.build())

        val valuePropertySpec = PropertySpec
            .builder(entry.value.getDataName(), entry.value.getListTypeName())
            .initializer("%L.map { %L.toUtil(it) }", keyDataName, entry.value.simpleName)
        addProperty(valuePropertySpec.build())

        csvDataMapCodeBlock.add("\n${entry.value.simpleName} to ${entry.value.getDataName()}")
        if (index != classNameMap.size) csvDataMapCodeBlock.add(",")
    }

    csvDataMapCodeBlock.add("\n)")
    val csvDataMapPropertySpec = PropertySpec.builder("csvDataMap", csvMapClassName)
        .initializer(csvDataMapCodeBlock.build())

    primaryConstructor(constructorBuilder.build())
        .addProperty(csvDataMapPropertySpec.build())

    return this
}