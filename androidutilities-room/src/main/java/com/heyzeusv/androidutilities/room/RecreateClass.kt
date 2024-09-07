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
import com.squareup.kotlinpoet.ksp.toTypeName

private val propertyNames = mutableListOf<String>()
private val stringListClass = ClassName("kotlin.collections", "List")
    .parameterizedBy(String::class.asTypeName())

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
                constructorBuilder.addParameter(name, prop.type.toTypeName())
                classBuilder.addProperty(
                    PropertySpec.builder(name, prop.type.toTypeName()).initializer(name).build()
                )
                propertyNames.add(name)
            }
        }
    }

    val propNamesSpec = PropertySpec.builder(::propertyNames.name, stringListClass)
        .initializer("listOf%L", propertyNames.joinToString(
            separator = "\", \"",
            prefix = "(\"",
            postfix = "\")"
        ))
        .build()
    classBuilder.addProperty(propNamesSpec)
    propertyNames.clear()

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
                constructorBuilder.addParameter(name, prop.type.toTypeName())
                this.addProperty(
                    PropertySpec.builder(name, prop.type.toTypeName()).initializer(name).build()
                )
                propertyNames.add(name)
            }
        }
    }
    return this
}