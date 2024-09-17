package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.RoomTypes.TO_ACCEPTED
import com.heyzeusv.androidutilities.room.RoomTypes.TO_COMPLEX
import com.heyzeusv.androidutilities.room.csv.CsvData
import com.heyzeusv.androidutilities.room.csv.CsvInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private val propertyInfoList = mutableListOf<PropertyInfo>()
private val fieldToTypeMap = mutableMapOf<String, String>()
private val stringMapClass = ClassName("kotlin.collections", "Map")
    .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())

internal fun recreateEntityClass(
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    classDeclaration: KSClassDeclaration,
    csvFileNames: MutableList<String>,
    logger: KSPLogger,
): TypeSpec.Builder {
    val classBuilder = TypeSpec
        .classBuilder(classDeclaration.utilName())
        .addModifiers(KModifier.DATA)
        .addSuperinterface(CsvData::class)
    val constructorBuilder = FunSpec.constructorBuilder()
    val companionTypeSpec = TypeSpec
        .companionObjectBuilder()
        .addSuperinterface(CsvInfo::class)
    val tableName =
        classDeclaration.annotations.find { it.shortName.getShortName() == "Entity" }
            ?.arguments?.find { it.name?.getShortName() == "tableName" }?.value.toString()
            .ifBlank { classDeclaration.simpleName.getShortName() }
    csvFileNames.add("$tableName.csv")
    classBuilder.recreateClass(
        constructorBuilder = constructorBuilder,
        classDeclaration = classDeclaration,
        logger = logger,
        tcInfoMap = tcInfoMap,
    )
    propertyInfoList.removeLast()

    val tableNamePropertySpec = PropertySpec.builder("tableName", String::class)
        .initializer("%S", tableName)
        .build()
    val toOriginalFun = FunSpec.builder("toOriginal")
        .returns(classDeclaration.toClassName())
        .addCode(buildCodeBlock {
            add("return ${classDeclaration.simpleName.getShortName()}(\n")
            indent()
            val infoIterator = propertyInfoList.iterator()
            handlePropertyInfoToOriginal(infoIterator, tcInfoMap, logger)
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
            handlePropertyInfoToUtil(infoIterator, tcInfoMap, logger)
            unindent()
            add(")")
        })

    val propertyToFieldNameSpec = PropertySpec.builder(::fieldToTypeMap.name, stringMapClass)
        .initializer(buildCodeBlock {
            add("mapOf(\n")
            var count = 0
            fieldToTypeMap.forEach { (field, type) ->
                count++
                add("%S to %S", field, type)
                if (count < fieldToTypeMap.size) add(",\n")
            }
            add("\n)")
        })
        .build()
    classBuilder.addProperty(tableNamePropertySpec)
    classBuilder.addProperty(propertyToFieldNameSpec)
    classBuilder.addCsvProperties(companionTypeSpec, tableName)
    fieldToTypeMap.clear()
    propertyInfoList.clear()

    classBuilder.addFunction(toOriginalFun.build())
    companionTypeSpec.addFunction(toUtilFun.build())
    classBuilder.addType(companionTypeSpec.build())
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
                val startType: TypeName = prop.type.toTypeName()
                val endType: TypeName
                if (TO_ACCEPTED.types.containsNullableType(prop.type.toTypeName())) {
                    endType = prop.type.toTypeName()
                } else {
                    val tcInfo = tcInfoMap[TO_COMPLEX]!!
                        .find { it.returnType.equalsNullableType(prop.type.toTypeName()) }!!
                    endType = tcInfo.parameterType
                }
                var fieldName = "$embeddedPrefix$name"
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                            ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = "$embeddedPrefix$columnName"
                }
                constructorBuilder.addParameter(fieldName, endType)
                this.addProperty(PropertySpec
                    .builder(fieldName, endType)
                    .initializer(fieldName)
                    .build()
                )
                val fieldInfo = FieldInfo(
                    name = name,
                    fieldName = fieldName,
                    startType = startType,
                    endType = endType
                )
                propertyInfoList.add(fieldInfo)
                fieldToTypeMap[fieldName] = endType.toString().removePrefix("kotlin.")
            }
        }
    }
    propertyInfoList.add(CloseClass())

    return this
}

private fun CodeBlock.Builder.handlePropertyInfoToOriginal(
    iterator: MutableIterator<PropertyInfo>,
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    logger: KSPLogger,
) {
    if (!iterator.hasNext()) return
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> {
            if (info.startType == info.endType) {
                add("%L = %L,\n", info.name, info.fieldName)
            } else {
                logger.info("start ${info.startType}, end ${info.endType}")
                val tcInfo = tcInfoMap[TO_COMPLEX]!!
                    .find { it.parameterType == info.endType && it.returnType == info.startType }!!
                val tcClass = ClassName(tcInfo.packageName, tcInfo.parentClass)
                add("%L = %T().%L(%L),\n", info.name, tcClass, tcInfo.functionName, info.fieldName)
            }
        }
        is EmbeddedInfo -> {
            add("%L = %L(\n", info.name, info.embeddedClass.simpleName.getShortName())
            indent()
            handlePropertyInfoToOriginal(iterator, tcInfoMap, logger)
        }
        is CloseClass -> {
            unindent()
            add("),\n")
        }
    }
    handlePropertyInfoToOriginal(iterator, tcInfoMap, logger)
}

private fun CodeBlock.Builder.handlePropertyInfoToUtil(
    iterator: MutableIterator<PropertyInfo>,
    tcInfoMap: Map<RoomTypes, MutableList<TypeConverterInfo>>,
    logger: KSPLogger,
    embeddedPrefixList: List<String> = emptyList(),
) {
    if (!iterator.hasNext()) return
    var removeLastPrefix = ""
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> {
            val embeddedPrefix = if (embeddedPrefixList.isEmpty()) {
                ""
            } else {
                embeddedPrefixList.joinToString(separator = ".", postfix = ".")
            }
            if (info.startType == info.endType) {
                add("%L = entity.$embeddedPrefix%L,\n", info.fieldName, info.name)
            } else {
                logger.info("start ${info.startType}, end ${info.endType}")
                val tcInfo = tcInfoMap[TO_ACCEPTED]!!
                    .find { it.parameterType == info.startType && it.returnType == info.endType }!!
                val tcClass = ClassName(tcInfo.packageName, tcInfo.parentClass)
                add("%L = %T().%L(entity.$embeddedPrefix%L), \n", info.fieldName, tcClass, tcInfo.functionName, info.name)
            }
        }
        is EmbeddedInfo -> handlePropertyInfoToUtil(
            iterator = iterator,
            tcInfoMap = tcInfoMap,
            logger = logger,
            embeddedPrefixList = embeddedPrefixList + info.name,
        )
        is CloseClass -> removeLastPrefix = embeddedPrefixList.last()
    }
    handlePropertyInfoToUtil(
        iterator = iterator,
        tcInfoMap = tcInfoMap,
        logger = logger,
        embeddedPrefixList = if (removeLastPrefix.isNotBlank()) {
            embeddedPrefixList - removeLastPrefix
        } else {
            embeddedPrefixList
        },
    )
}

private fun TypeSpec.Builder.addCsvProperties(
    companionTypeSpec: TypeSpec.Builder,
    tableName: String,
) {
    val fileNamePropSpec = PropertySpec.builder("csvFileName", String::class)
        .addModifiers(KModifier.OVERRIDE)
        .initializer("%S", "$tableName.csv")
        .build()
    val stringListClass = ClassName("kotlin.collections", "List")
        .parameterizedBy(String::class.asTypeName())
    val headerPropSpec = PropertySpec.builder("csvHeader", stringListClass)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(buildCodeBlock {
            add("listOf(\n")
            fieldToTypeMap.keys.forEach {
                add("%S, ", it)
            }
            add("\n)")
        })
        .build()
    val anyListClass = ClassName("kotlin.collections", "List")
        .parameterizedBy(Any::class.asTypeName().copy(nullable = true))
    val csvRow = PropertySpec.builder("csvRow", anyListClass)
        .addModifiers(KModifier.OVERRIDE)
        .initializer(buildCodeBlock {
            add("listOf(\n")
            fieldToTypeMap.keys.forEach {
                add("%L, ", it)
            }
            add("\n)")
        })
        .build()
    addProperty(csvRow)
    companionTypeSpec.addProperty(fileNamePropSpec).addProperty(headerPropSpec)
}