package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
    )

    val toOriginalFun = FunSpec.builder("toOriginal")
        .returns(classDeclaration.toClassName())
        .addCode(buildCodeBlock {
            add("return ${classDeclaration.simpleName.getShortName()}(\n")
            indent()
            val infoIterator = propertyInfoList.iterator()
            handlePropertyInfoToOriginal(infoIterator)
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
            handlePropertyInfoToUtil(infoIterator)
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
                    embeddedPrefix = "$embeddedPrefix$newPrefix",
                )
            } else {
                var fieldName = "$embeddedPrefix$name"
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                            ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = "$embeddedPrefix$columnName"
                }
                constructorBuilder.addParameter(fieldName, prop.type.toTypeName())
                this.addProperty(PropertySpec
                    .builder(fieldName, prop.type.toTypeName())
                    .initializer(fieldName)
                    .build()
                )
                val fieldInfo = FieldInfo(
                    name = name,
                    fieldName = fieldName,
                )
                propertyInfoList.add(fieldInfo)
                fieldToPropertyName[fieldName] = name
            }
        }
    }

    return this
}

fun CodeBlock.Builder.handlePropertyInfoToOriginal(iterator: MutableIterator<PropertyInfo>) {
    if (!iterator.hasNext()) return
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> add("%L = %L,\n", info.name, info.fieldName)
        is EmbeddedInfo -> {
            add("%L = %L(\n", info.name, info.embeddedClass.simpleName.getShortName())
            indent()
            handlePropertyInfoToOriginal(iterator)
            unindent()
            add(")\n")
        }
    }
    handlePropertyInfoToOriginal(iterator)
}

fun CodeBlock.Builder.handlePropertyInfoToUtil(
    iterator: MutableIterator<PropertyInfo>,
    embeddedPrefix: String = "",
) {
    if (!iterator.hasNext()) return
    when (val info: PropertyInfo = iterator.next()) {
        is FieldInfo -> {
            add("%L = entity.$embeddedPrefix%L,\n", info.fieldName, info.name)
        }
        is EmbeddedInfo -> {
            handlePropertyInfoToUtil(iterator, "$embeddedPrefix${info.name}.")
        }
    }
    handlePropertyInfoToUtil(iterator, embeddedPrefix)
}
