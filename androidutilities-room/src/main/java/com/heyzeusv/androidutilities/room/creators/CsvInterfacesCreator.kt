package com.heyzeusv.androidutilities.room.creators

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.heyzeusv.androidutilities.room.util.Constants.CSV_DATA
import com.heyzeusv.androidutilities.room.util.Constants.CSV_INFO
import com.heyzeusv.androidutilities.room.util.Constants.EXTENSION_KT
import com.heyzeusv.androidutilities.room.util.asListTypeName
import com.heyzeusv.androidutilities.room.util.getPackageName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

internal class CsvInterfacesCreator(
    private val codeGenerator: CodeGenerator,
    private val dbClassDeclaration: KSClassDeclaration,
    private val logger: KSPLogger,
) {
    private val packageName = dbClassDeclaration.getPackageName()

    private fun createCsvDataFile() {
        logger.info("Creating CsvData...")
        val fileBuilder = FileSpec.builder(packageName, CSV_DATA)

        val interfaceBuilder = TypeSpec.interfaceBuilder(CSV_DATA)
            .buildCsvData()

        fileBuilder.addType(interfaceBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = CSV_DATA,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    private fun createCsvInfoFile() {
        logger.info("Creating CsvInfo...")
        val fileBuilder = FileSpec.builder(packageName, CSV_INFO)

        val interfaceBuilder = TypeSpec.interfaceBuilder(CSV_INFO)
            .buildCsvInfo()

        fileBuilder.addType(interfaceBuilder.build())

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, dbClassDeclaration.containingFile!!),
            packageName = packageName,
            fileName = CSV_INFO,
            extensionName = EXTENSION_KT,
        ).bufferedWriter().use { fileBuilder.build().writeTo(it) }
    }

    /**
     *  Builds CsvData TypeSpec by adding properties.
     */
    private fun TypeSpec.Builder.buildCsvData(): TypeSpec.Builder {
        addProperty("csvRow", Any::class.asTypeName().copy(nullable = true).asListTypeName())

        return this
    }

    /**
     *  Builds CsvInfo TypeSpec by adding properties.
     */
    private fun TypeSpec.Builder.buildCsvInfo(): TypeSpec.Builder {
        val stringMapClassName = ClassName("kotlin.collections", "Map")
            .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())
        addProperty("csvFileName", String::class)
        addProperty("csvFieldToTypeMap", stringMapClassName)

        return this
    }

    init {
        createCsvDataFile()
        createCsvInfoFile()
    }
}