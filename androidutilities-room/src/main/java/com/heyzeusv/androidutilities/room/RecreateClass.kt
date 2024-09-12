package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.RoomTypes.TO_ACCEPTED
import com.heyzeusv.androidutilities.room.RoomTypes.TO_COMPLEX
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private val propertyInfoList = mutableListOf<PropertyInfo>()
private val fieldToPropertyName = mutableMapOf<String, String>()
private val stringMapClass = ClassName("kotlin.collections", "Map")
    .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())

internal fun recreateEntityClass(
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): TypeSpec.Builder {
    val classBuilder = TypeSpec
        .classBuilder(classDeclaration.utilName())
        .addModifiers(KModifier.DATA)
    val constructorBuilder = FunSpec.constructorBuilder()
    val companion = TypeSpec.companionObjectBuilder()

    classBuilder.recreateClass(
        constructorBuilder = constructorBuilder,
        classDeclaration = classDeclaration,
        logger = logger,
        tcInfoMap = tcInfoMap,
    )

    val toOriginalFun = FunSpec.builder("toOriginal")
        .returns(classDeclaration.toClassName())
        .addCode(buildCodeBlock {
            add("return ${classDeclaration.simpleName.getShortName()}(\n")
            indent()
            val infoIterator = propertyInfoList.iterator()
            handlePropertyInfoToOriginal(infoIterator, tcInfoMap)
            unindent()
            add(")")
        })
    val toUtilFun = FunSpec.builder("toUtil")
        .returns(ClassName(classDeclaration.packageName.asString(), classDeclaration.utilName()))
        .addParameter("entity", classDeclaration.toClassName())
        .addCode(buildCodeBlock {
            add("return ${classDeclaration.utilName()}(\n")
            indent()
            val infoIterator = propertyInfoList.iterator()
            handlePropertyInfoToUtil(infoIterator, tcInfoMap)
            unindent()
            add(")")
        })

    val propertyToFieldNameSpec = PropertySpec.builder(::fieldToPropertyName.name, stringMapClass)
        .initializer(buildCodeBlock {
            add("mapOf(\n")
            var count = 0
            fieldToPropertyName.forEach { (property, field) ->
                count++
                add("%S to %S", property, field)
                if (count < fieldToPropertyName.size) add(",\n")
            }
            add("\n)")
        })
        .build()
    classBuilder.addProperty(propertyToFieldNameSpec)
    fieldToPropertyName.clear()
    propertyInfoList.clear()

    classBuilder.addFunction(toOriginalFun.build())
    companion.addFunction(toUtilFun.build())
    classBuilder.addType(companion.build())
    return classBuilder.primaryConstructor(constructorBuilder.build())
}

private fun TypeSpec.Builder.recreateClass(
    constructorBuilder: FunSpec.Builder,
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    embeddedPrefix: String = "",
): TypeSpec.Builder {
    classDeclaration.getAllProperties().forEach { prop ->
        prop.qualifiedName?.getShortName()?.let { name ->
            val annotationNames = prop.annotations.map { it.shortName.getShortName() }
            if (annotationNames.contains("Ignore")) {
                logger.info("Ignoring $name of type ${prop.type}")
            } else if (annotationNames.contains("Embedded")) {
                logger.info("Embedded class ${prop.type}")
                val embeddedClass = prop.type.resolve().declaration as KSClassDeclaration
                val newPrefix = prop.annotations.find { it.shortName.getShortName() == "Embedded" }
                    ?.arguments?.find { it.name?.getShortName() == "prefix" }?.value.toString()
                val embeddedInfo = EmbeddedInfo(
                    name = name,
                    embeddedClass = embeddedClass,
                )
                propertyInfoList.add(embeddedInfo)
                this.recreateClass(
                    constructorBuilder = constructorBuilder,
                    classDeclaration = embeddedClass,
                    logger = logger,
                    tcInfoMap = tcInfoMap,
                    embeddedPrefix = "$embeddedPrefix$newPrefix",
                )
            } else {
                val type = if (TO_ACCEPTED.types.containsNullableType(prop.type.toTypeName())) {
                    prop.type.toTypeName()
                } else {
                    val tcInfo = tcInfoMap[TO_COMPLEX]!!
                        .find { it.returnType.equalsNullableType(prop.type.toTypeName()) }!!
                    tcInfo.parameterType
                }
                var fieldName = "$embeddedPrefix$name"
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                            ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = "$embeddedPrefix$columnName"
                }
                constructorBuilder.addParameter(fieldName, type)
                this.addProperty(PropertySpec
                    .builder(fieldName, type)
                    .initializer(fieldName)
                    .build()
                )
                val fieldInfo = FieldInfo(
                    name = name,
                    fieldName = fieldName,
                    type = prop.type.toTypeName()
                )
                propertyInfoList.add(fieldInfo)
                fieldToPropertyName[fieldName] = name
            }
        }
    }

    return this
}

private fun CodeBlock.Builder.handlePropertyInfoToOriginal(
    iterator: MutableIterator<PropertyInfo>,
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
) {
    if (!iterator.hasNext()) return
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> add("%L = %L,\n", info.name, info.fieldName)
        is EmbeddedInfo -> {
            add("%L = %L(\n", info.name, info.embeddedClass.simpleName.getShortName())
            indent()
            handlePropertyInfoToOriginal(iterator, tcInfoMap)
            unindent()
            add(")\n")
        }
    }
    handlePropertyInfoToOriginal(iterator, tcInfoMap)
}

private fun CodeBlock.Builder.handlePropertyInfoToUtil(
    iterator: MutableIterator<PropertyInfo>,
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    embeddedPrefix: String = "",
) {
    if (!iterator.hasNext()) return
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> add("%L = entity.$embeddedPrefix%L,\n", info.fieldName, info.name)
        is EmbeddedInfo -> handlePropertyInfoToUtil(
            iterator = iterator,
            tcInfoMap = tcInfoMap,
            embeddedPrefix = "$embeddedPrefix${info.name}.",
        )
    }
    handlePropertyInfoToUtil(
        iterator = iterator,
        tcInfoMap = tcInfoMap,
        embeddedPrefix = embeddedPrefix,
    )
}
