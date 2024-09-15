package com.heyzeusv.androidutilities.room.csv

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal const val CONTEXT_PROP = "context"
internal val contextClassName = ClassName("android.content", "Context")

internal fun buildCsvConverter(
    codeGenerator: CodeGenerator,
    dbClass: KSClassDeclaration,
    logger: KSPLogger,
) {
    val packageName = dbClass.containingFile?.packageName?.asString().orEmpty()
    val fileName = "CsvConverter"

    val fileSpecBuilder = FileSpec.builder("$packageName.csv", fileName)

    val classBuilder = TypeSpec
        .classBuilder(fileName)
        .primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(CONTEXT_PROP, contextClassName)
            .build()
        )
        .addProperty(PropertySpec.builder(CONTEXT_PROP, contextClassName)
            .initializer(CONTEXT_PROP)
            .addModifiers(KModifier.PRIVATE)
            .build()
        )

    classBuilder.addFunction(exportRoomToCsvFunSpec().build())
    classBuilder.addFunction(exportRoomEntityToCsvFunSpec().build())
    classBuilder.addFunction(createNewExportDirectoryFunSpec().build())
    classBuilder.addFunction(findOrCreateParentDirectoryFunSpec().build())
    fileSpecBuilder.addType(classBuilder.build())

    codeGenerator.createNewFile(
        dependencies = Dependencies(false, dbClass.containingFile!!),
        packageName = packageName,
        fileName = fileName,
        extensionName = "kt",
    ).bufferedWriter().use { fileSpecBuilder.build().writeTo(it) }
}

fun CodeBlock.Builder.addIndented(code: CodeBlock.Builder.() -> Unit): CodeBlock.Builder = apply {
    indent()
    code()
    unindent()
}