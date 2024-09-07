package com.heyzeusv.androidutilities.room

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName

private val propertyToFieldName = mutableMapOf<String, String>()
private val stringMapClass = ClassName("kotlin.collections", "Map")
    .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())

internal fun recreateClass(
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): TypeSpec.Builder {
    val classBuilder = TypeSpec
        .classBuilder(classDeclaration.utilName())
        .addModifiers(KModifier.DATA)
    val constructorBuilder = FunSpec.constructorBuilder()

    classDeclaration.getAllProperties().forEach { prop ->
        prop.qualifiedName?.getShortName()?.let { name ->
            val annotationNames = prop.annotations.map { it.shortName.getShortName() }
            if (annotationNames.contains("Ignore")) {
                logger.info("Ignoring $name of type ${prop.type}")
            } else if (annotationNames.contains("Embedded")) {
                logger.info("Embedded class ${prop.type}")
                val embeddedClass = prop.type.resolve().declaration as KSClassDeclaration
                classBuilder.handleEmbeddedClass(constructorBuilder, embeddedClass, logger)
            } else {
                var fieldName = name
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                        ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = columnName
                }
                constructorBuilder.addParameter(name, prop.type.toTypeName())
                classBuilder.addProperty(
                    PropertySpec.builder(name, prop.type.toTypeName()).initializer(name).build()
                )
                propertyToFieldName[name] = fieldName
            }
        }
    }

    val propertyToFieldNameSpec = PropertySpec.builder(::propertyToFieldName.name, stringMapClass)
        .initializer(buildCodeBlock {
            add("mapOf(\n")
            var count = 0
            propertyToFieldName.forEach { (property, field) ->
                count++
                add("%S to %S", property, field)
                if (count < propertyToFieldName.size) add(",\n")
            }
            add("\n)")
        })
        .build()
    propertyToFieldName.entries.joinToString()
    classBuilder.addProperty(propertyToFieldNameSpec)
    propertyToFieldName.clear()

    return classBuilder.primaryConstructor(constructorBuilder.build())
}

private fun TypeSpec.Builder.handleEmbeddedClass(
    constructorBuilder: FunSpec.Builder,
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
): TypeSpec.Builder {
    classDeclaration.getAllProperties().forEach { prop ->
        prop.qualifiedName?.getShortName()?.let { name ->
            val annotationNames = prop.annotations.map { it.shortName.getShortName() }
            if (annotationNames.contains("Ignore")) {
                logger.info("Ignoring $name of type ${prop.type}")
            } else if (annotationNames.contains("Embedded")) {
                logger.info("Embedded class ${prop.type}")
                val embeddedClass = prop.type.resolve().declaration as KSClassDeclaration
                this.handleEmbeddedClass(constructorBuilder, embeddedClass, logger)
            } else {
                var fieldName = name
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                            ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = columnName
                }
                constructorBuilder.addParameter(name, prop.type.toTypeName())
                this.addProperty(
                    PropertySpec.builder(name, prop.type.toTypeName()).initializer(name).build()
                )
                propertyToFieldName[name] = fieldName
            }
        }
    }
    return this
}