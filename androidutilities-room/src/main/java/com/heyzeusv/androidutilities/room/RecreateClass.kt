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

    classBuilder.recreateClass(constructorBuilder, classDeclaration, logger)

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
    fieldToPropertyName.entries.joinToString()
    classBuilder.addProperty(propertyToFieldNameSpec)
    fieldToPropertyName.clear()

    return classBuilder.primaryConstructor(constructorBuilder.build())
}

private fun TypeSpec.Builder.recreateClass(
    constructorBuilder: FunSpec.Builder,
    classDeclaration: KSClassDeclaration,
    logger: KSPLogger,
    embeddedPrefix: String = ""
): TypeSpec.Builder {
    classDeclaration.getAllProperties().forEach { prop ->
        prop.qualifiedName?.getShortName()?.let { name ->
            val annotationNames = prop.annotations.map { it.shortName.getShortName() }
            if (annotationNames.contains("Ignore")) {
                logger.info("Ignoring $name of type ${prop.type}")
            } else if (annotationNames.contains("Embedded")) {
                logger.info("Embedded class ${prop.type}")
                val embeddedClass = prop.type.resolve().declaration as KSClassDeclaration
                val prefix = prop.annotations.find { it.shortName.getShortName() == "Embedded" }
                    ?.arguments?.find { it.name?.getShortName() == "prefix" }?.value.toString()
                this.recreateClass(constructorBuilder, embeddedClass, logger, prefix)
            } else {
                var fieldName = "$embeddedPrefix$name"
                if (annotationNames.contains("ColumnInfo")) {
                    val columnName =
                        prop.annotations.find { it.shortName.getShortName() == "ColumnInfo" }
                            ?.arguments?.find { it.name?.getShortName() == "name" }?.value.toString()
                    if (columnName != "[field-name]") fieldName = "$embeddedPrefix$columnName"
                }
                constructorBuilder.addParameter("$embeddedPrefix$name", prop.type.toTypeName())
                this.addProperty(PropertySpec
                    .builder("$embeddedPrefix$name", prop.type.toTypeName())
                    .initializer("$embeddedPrefix$name")
                    .build()
                )
                fieldToPropertyName[fieldName] = name
            }
        }
    }

    return this
}